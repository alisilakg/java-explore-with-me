package ru.practicum.explore.event.service;

import ru.practicum.explore.enums.EventSort;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.event.dto.*;
import ru.practicum.explore.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.request.dto.EventRequestStatusUpdateResult;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getAllPublishedEvents(String text,
                                              List<Long> categories,
                                              Boolean paid,
                                              LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd,
                                              boolean onlyAvailable,
                                              EventSort sort,
                                              int from,
                                              int size,
                                              String ip,
                                              String url);

    EventFullDto getFullEventById(Long userId, Long eventId);

    List<EventShortDto> getShortEventsByUserId(Long userId, Integer from, Integer size);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventRequestStatusUpdateResult updateByPrivate(EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest,
                                                   Long userId,
                                                   Long eventId);

    List<EventFullDto> getAllEventsByAdmin(List<Long> users,
                                           List<EventState> states,
                                           List<Long> categories,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           int from,
                                           int size);

    EventFullDto patchEventByAdmin(long eventId, UpdateEventAdminRequest updateEventDto);

    EventFullDto getPublishedEventById(Long eventId, String ip, String url);
}
