package ru.practicum.explore.event.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.explore.StatsClient;
import ru.practicum.explore.category.mapper.CategoryMapper;
import ru.practicum.explore.category.service.CategoryService;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.event.dto.EventFullDto;
import ru.practicum.explore.event.dto.EventShortDto;
import ru.practicum.explore.event.dto.NewEventDto;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.request.service.RequestService;
import ru.practicum.explore.user.mapper.UserMapper;
import ru.practicum.explore.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class EventMapper {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private final CategoryService categoryService;
    private final UserService userService;
    private final RequestService requestService;
    private final StatsClient statClient;

    @Autowired
    public EventMapper(CategoryService categoryService, UserService userService, RequestService requestService, StatsClient statClient) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.requestService = requestService;
        this.statClient = statClient;
    }

    public Event toEvent(NewEventDto newEventDto, Long initiatorId) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(categoryService.findCategoryByIdForMapping(newEventDto.getCategory()))
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .initiator(userService.findUserByIdForMapping(initiatorId))
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .state(EventState.PENDING)
                .title(newEventDto.getTitle())
                .build();
    }

    public EventFullDto toEventFullDto(Event event) {
        EventFullDto eventFullDto = EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(requestService.countConfirmedRequestsByEventId(event.getId()))
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(countView(event))
                .build();
        if (event.getPublishedOn() != null) {
            eventFullDto.setPublishedOn(event.getPublishedOn());
        }
        return eventFullDto;
    }

    public List<EventFullDto> toEventFullDto(Iterable<Event> events) {
        List<EventFullDto> result = new ArrayList<>();

        for (Event event : events) {
            result.add(toEventFullDto(event));
        }

        return result;
    }

    public EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(requestService.countConfirmedRequestsByEventId(event.getId()))
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(countView(event))
                .build();
    }

    public List<EventShortDto> toEventShortDto(Iterable<Event> events) {
        List<EventShortDto> result = new ArrayList<>();

        for (Event event : events) {
            result.add(toEventShortDto(event));
        }
        return result;
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

