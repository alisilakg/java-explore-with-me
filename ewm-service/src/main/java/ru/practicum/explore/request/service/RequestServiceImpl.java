package ru.practicum.explore.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.enums.RequestStatus;
import ru.practicum.explore.error.exception.ConflictException;
import ru.practicum.explore.error.exception.NotFoundException;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.repository.EventRepository;
import ru.practicum.explore.request.dto.ParticipationRequestDto;
import ru.practicum.explore.request.mapper.RequestMapper;
import ru.practicum.explore.request.model.Request;
import ru.practicum.explore.request.repository.RequestRepository;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Заявка на участие уже создана.");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с ID=" + eventId + " не найдено"));
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException(
                    String.format("Инициатор события с id %d не может добавить запрос на участие в своём событии с id %d",
                            userId, eventId));
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException(String.format("Нельзя участвовать в неопубликованном событии с id %d", eventId));
        }
        if (event.getParticipantLimit() > 0) {
            Long participants = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            Long limit = event.getParticipantLimit();
            if (participants >= limit) {
                throw new ConflictException(String.format("У события с id  %d достигнут лимит запросов на участие",
                        eventId));
            }
        }
        Request newRequest = completeNewRequest(userId, event);
        return RequestMapper.toRequestDto(requestRepository.save(newRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAllRequestsByUser(Long userId) {
        getUserIfExists(userId);
        List<Request> requests = requestRepository.findByRequesterId(userId);
        return RequestMapper.toRequestDto(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        getUserIfExists(userId);
        Request request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException(String.format("Заявка с ID=%d  не найдена", requestId)));
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByPrivate(Long userId, Long eventId) {
        getUserIfExists(userId);
        return requestRepository.findByEventId(eventId)
                .stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    private Request completeNewRequest(Long userId, Event event) {
        User user = getUserIfExists(userId);
        boolean needConfirmation = event.getRequestModeration();
        boolean hasParticipantsLimit = event.getParticipantLimit() != 0;
        RequestStatus status = needConfirmation && hasParticipantsLimit ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        return Request.builder()
                .requester(user)
                .status(status)
                .event(event)
                .created(LocalDateTime.now())
                .build();
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не зарегестрирован"));
    }
}
