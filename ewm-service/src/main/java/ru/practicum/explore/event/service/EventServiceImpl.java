package ru.practicum.explore.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.StatsClient;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.category.repository.CategoryRepository;
import ru.practicum.explore.category.service.CategoryService;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.enums.*;
import ru.practicum.explore.error.exception.ConflictException;
import ru.practicum.explore.error.exception.NotFoundException;
import ru.practicum.explore.error.exception.ValidationException;
import ru.practicum.explore.event.dto.*;
import ru.practicum.explore.event.mapper.EventMapper;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.repository.EventRepository;
import ru.practicum.explore.location.model.Location;
import ru.practicum.explore.location.repository.LocationRepository;
import ru.practicum.explore.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore.request.mapper.RequestMapper;
import ru.practicum.explore.request.model.Request;
import ru.practicum.explore.request.repository.RequestRepository;
import ru.practicum.explore.request.service.RequestService;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.repository.UserRepository;
import ru.practicum.explore.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statClient;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final RequestService requestService;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }
        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(0L);
        }
        if (newEventDto.getPaid() == null) {
            newEventDto.setPaid(false);
        }
        Location location = getLocation(newEventDto.getLocation());
        newEventDto.setLocation(location);
        Category category = categoryService.findCategoryByIdForMapping(newEventDto.getCategory());
        User user = userService.findUserByIdForMapping(userId);
        Event newEvent = EventMapper.toEvent(newEventDto, user, category);
        Event savedEvent = eventRepository.save(newEvent);
        Event eventWithViwesAndRequests = getEventWithViewsAndCountRequests(savedEvent);
        return EventMapper.toEventFullDto(eventWithViwesAndRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getFullEventById(Long userId, Long eventId) {
        checkUser(userId);
        Event event = getEventIfExists(eventId);
        Event eventWithViwesAndRequests = getEventWithViewsAndCountRequests(event);
        return EventMapper.toEventFullDto(eventWithViwesAndRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getShortEventsByUserId(Long userId, Integer from, Integer size) {
        checkUser(userId);
        int page = from / size;

        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(page, size));
        List<Event> eventsWithViewsAndRequests = getEventsWithViewsAndCountRequests(events);
        return eventsWithViewsAndRequests.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        checkUser(userId);
        Event event = getEventIfExists(eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменять опубликованные события.");
        }
        updateEventFields(event, updateEventUserRequest);
        updateEventStateAction(event, updateEventUserRequest.getStateAction());
        Event savedEvent = eventRepository.save(event);
        Event eventWithViwesAndRequests = getEventWithViewsAndCountRequests(savedEvent);
        return EventMapper.toEventFullDto(eventWithViwesAndRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllEventsByAdmin(List<Long> users,
                                                  List<EventState> states,
                                                  List<Long> categories,
                                                  LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd,
                                                  int from,
                                                  int size) {
        if (users != null && users.size() == 1 && users.get(0).equals(0L)) {
            users = null;
        }

        List<Event> events = eventRepository.findAllEventsByAdmin(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                PageRequest.of(from, size));
        List<Event> eventsWithViewsAndRequests = getEventsWithViewsAndCountRequests(events);
        List<EventFullDto> eventDtos = EventMapper.toEventFullDto(eventsWithViewsAndRequests);

        return eventDtos.stream().peek(eventDto -> eventDto.setConfirmedRequests(
                        requestRepository.getCountByEventIdAndState(eventDto.getId(), RequestStatus.CONFIRMED)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto patchEventByAdmin(long eventId, UpdateEventAdminRequest updateEventDto) {
        Event event = getEventIfExists(eventId);
        LocalDateTime actual = event.getEventDate();
        checkDateTimeIsAfterNowWithGap(actual, 1);
        LocalDateTime target = updateEventDto.getEventDate();
        if (Objects.nonNull(target)) {
            checkDateTimeIsAfterNowWithGap(target, 2);
        }
        StateActionAdmin action = updateEventDto.getStateAction();
        if (Objects.nonNull(action)) {
            switch (action) {
                case PUBLISH_EVENT:
                    publishEvent(updateEventDto, event);
                    break;
                case REJECT_EVENT:
                    rejectEvent(event);
                    break;
            }
        }
        Event savedEvent = eventRepository.save(event);
        Event eventWithViwesAndRequests = getEventWithViewsAndCountRequests(savedEvent);
        return EventMapper.toEventFullDto(eventWithViwesAndRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllPublishedEvents(String text,
                                                     List<Long> categories,
                                                     Boolean paid,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     boolean onlyAvailable,
                                                     EventSort sort,
                                                     int from,
                                                     int size,
                                                     String ip,
                                                     String url) {
        saveHit(ip, url);
        if (rangeStart != null || rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new ValidationException("Время окончания не должно быть позднее времени начала.");
            }
        }
        EventFilterParams params = EventFilterParams.builder()
                    .states(List.of(EventState.PUBLISHED))
                    .categories(categories)
                    .rangeStart(rangeStart)
                    .rangeEnd(rangeEnd)
                    .from(from)
                    .size(size)
                    .text(text)
                    .paid(paid)
                    .build();
        List<Event> events = eventRepository.findAllEventsByParams(params);
        List<Event> eventsWithViewsAndRequests = getEventsWithViewsAndCountRequests(events);
        return eventsWithViewsAndRequests.stream().map(EventMapper::toEventShortDto)
                .sorted(getComparator(sort)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublishedEventById(Long eventId, String ip, String url) {
        saveHit(ip, url);

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id=" + eventId + " не найдено."));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено.");
        }
        Event savedEvent = eventRepository.save(event);
        Event eventWithViwesAndRequests = getEventWithViewsAndCountRequests(savedEvent);
        return EventMapper.toEventFullDto(eventWithViwesAndRequests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateByPrivate(EventRequestStatusUpdateRequest update, Long userId, Long eventId) {
        checkUser(userId);
        Event event = getEventIfExists(eventId);
        List<Long> requestIds = update.getRequestIds();
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (!isRequestStatusUpdateAllowed(event, update)) {
            return result;
        }
        List<Request> requestsToUpdate = requestRepository.findAllByIdIn(requestIds);
        checkAllRequestsPending(requestsToUpdate);
        RequestStatus status = RequestStatus.valueOf(update.getStatus());
        if (status == RequestStatus.CONFIRMED) {
            confirmAndSetInResult(requestsToUpdate, result, event);
        } else if (status == RequestStatus.REJECTED) {
            rejectAndSetInResult(requestsToUpdate, result);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Event findEventByIdForMapping(Long eventId) {
        return getEventWithViewsAndCountRequests(getEventIfExists(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsWithViewsAndCountRequests(List<Event> events) {
        for (Event event : events) {
         getEventWithViewsAndCountRequests(event);
        }
        return events;
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventWithViewsAndCountRequests(Event savedEvent) {
        if (savedEvent.getState() == EventState.PUBLISHED) {
            Long countConfirmedRequests = requestService.countConfirmedRequestsByEventId(savedEvent.getId());
            savedEvent.setConfirmedRequests(countConfirmedRequests);
            Long views = countView(savedEvent);
            savedEvent.setViews(views);
        } else {
            Long countConfirmedRequests = 0L;
            savedEvent.setConfirmedRequests(countConfirmedRequests);
            Long views = 0L;
            savedEvent.setViews(views);
        }

        return savedEvent;
    }

    private Comparator<EventShortDto> getComparator(EventSort sortType) {
        return EventShortDto.getComparator(sortType);
    }

    private void publishEvent(UpdateEventAdminRequest request, Event event) {
        EventState state = event.getState();
        if (state == EventState.PUBLISHED) {
            throw new ConflictException("Событие уже было опубликовано.");
        }
        if (state == EventState.CANCELED) {
            throw new ConflictException("Событие уже было отменено.");
        }
        updateEventFields(event, request);
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        EventState state = event.getState();
        if (state == EventState.PUBLISHED) {
            throw new ConflictException("Вы не можете отменить событие.");
        }
        event.setState(EventState.CANCELED);
    }

    private void updateEventFields(Event event, UpdateEventDto request) {
        updateEventAnnotation(event, request.getAnnotation());
        updateEventCategory(event, request.getCategory());
        updateEventDescription(event, request.getDescription());
        updateEventDate(event, request.getEventDate());
        updateEventLocation(event, request.getLocation());
        updateEventPaidStatus(event, request.getPaid());
        updateEventParticipationLimit(event, request.getParticipantLimit());
        updateEventRequestModeration(event, request.getRequestModeration());
        updateEventTitle(event, request.getTitle());
    }

    private void updateEventTitle(Event event, String title) {
        if (Objects.nonNull(title) && !title.isBlank()) {
            event.setTitle(title);
        }
    }

    private void updateEventStateAction(Event event, StateActionUser action) {
        if (Objects.nonNull(action)) {
            if (action == StateActionUser.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (action == StateActionUser.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private void updateEventRequestModeration(Event event, Boolean requestModeration) {
        if (Objects.nonNull(requestModeration)) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateEventParticipationLimit(Event event, Long limit) {
        if (Objects.nonNull(limit)) {
            event.setParticipantLimit(limit);
        }
    }

    private void updateEventPaidStatus(Event event, Boolean paid) {
        if (Objects.nonNull(paid)) {
            event.setPaid(paid);
        }
    }

    private void updateEventLocation(Event event, Location location) {
        if (Objects.nonNull(location)) {
            Location updatedLocation = getLocation(location);
            event.setLocation(updatedLocation);
        }
    }

    private void updateEventDate(Event event, LocalDateTime eventDate) {
        if (Objects.nonNull(eventDate)) {
            checkDateTimeIsAfterNowWithGap(eventDate, 1);
            event.setEventDate(eventDate);
        }
    }

    private void updateEventDescription(Event event, String description) {
        if (Objects.nonNull(description) && !description.isBlank()) {
            event.setDescription(description);
        }
    }

    private void updateEventCategory(Event event, Long catId) {
        if (Objects.nonNull(catId)) {
            Category updated = getCategoryIfExists(catId);
            event.setCategory(updated);
        }
    }

    private void updateEventAnnotation(Event event, String annotation) {
        if (Objects.nonNull(annotation) && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
    }

    private void checkDateTimeIsAfterNowWithGap(LocalDateTime value, Integer gapFromNowInHours) {
        LocalDateTime minValidDateTime = LocalDateTime.now().plusHours(gapFromNowInHours);
        if (value.isBefore(minValidDateTime)) {
            throw new ValidationException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации.");
        }
    }

    private void saveHit(String ip, String url) {
        String app = "ewm-main-service";
        EndpointHitDto endpointHitDto = new EndpointHitDto(null, app, url, ip, LocalDateTime.now());
        statClient.postHits(endpointHitDto);
    }

    private Location getLocation(Location location) {
        return locationRepository.getByLatAndLon(location.getLat(), location.getLon())
                .orElse(locationRepository.save(location));
    }

    private boolean isRequestStatusUpdateAllowed(Event event, EventRequestStatusUpdateRequest update) {
        return event.getRequestModeration() &&
                event.getParticipantLimit() > 0 &&
                !update.getRequestIds().isEmpty();
    }

    private static void checkAllRequestsPending(List<Request> requests) {
        boolean allPending = requests.stream()
                .allMatch(r -> r.getStatus() == RequestStatus.PENDING);
        if (!allPending) {
            throw new ConflictException("Невозможно изменить статус запроса.");
        }
    }

    private void confirmAndSetInResult(List<Request> requestsToUpdate, EventRequestStatusUpdateResult result, Event event) {
        long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long limit = event.getParticipantLimit();

        for (Request request : requestsToUpdate) {
            if (confirmed == limit) {
                int start = requestsToUpdate.indexOf(request);
                int end = requestsToUpdate.size();
                rejectAndSetInResult(requestsToUpdate.subList(start, end), result);
                throw new ConflictException("Лимит участников достигнут.");
            }
            confirmAndSetInResult(List.of(request), result);
            confirmed++;
        }
    }

    private void confirmAndSetInResult(List<Request> requestsToUpdate, EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.CONFIRMED);
        List<Request> confirmed = requestRepository.saveAll(requestsToUpdate);
        result.setConfirmedRequests(RequestMapper.toRequestDto(confirmed));
    }

    private void rejectAndSetInResult(List<Request> requestsToUpdate, EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.REJECTED);
        List<Request> rejectedRequests = requestRepository.saveAll(requestsToUpdate);
        result.setRejectedRequests(RequestMapper.toRequestDto(rejectedRequests));
    }

    private void setStatus(List<Request> requestsToUpdate, RequestStatus status) {
        requestsToUpdate.forEach(r -> r.setStatus(status));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено."));
    }

    private void checkUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не зарегестрирован"));
    }

    private Category getCategoryIfExists(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена."));
    }

    private long countView(Event event) {
        long views = 0L;
        if (event.getState() == EventState.PUBLISHED) {
            List<ViewStatsDto> viewStatsDto = getEventsViewsList(List.of(event));
            views = viewStatsDto.isEmpty() ? 0 : (int) viewStatsDto.get(0).getHits();
        }
        return views;
    }

    private List<ViewStatsDto> getEventsViewsList(List<Event> events) {
        List<String> eventUris = events
                .stream()
                .map(e -> String.format("/events/%s", e.getId()))
                .collect(Collectors.toList());
        LocalDateTime start = events.get(0).getCreatedOn();
        for (Event event : events) {
            if (event.getCreatedOn().isBefore(start)) {
                start = event.getCreatedOn();
            }
        }
        LocalDateTime end = LocalDateTime.now();
        String app = "ewm-main-service";
        Boolean unique = true;

        return statClient.getStats(start.format(DATE_FORMATTER), end.format(DATE_FORMATTER), eventUris, unique, app);
    }

}
