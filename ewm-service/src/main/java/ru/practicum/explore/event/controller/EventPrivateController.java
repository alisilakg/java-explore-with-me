package ru.practicum.explore.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.event.dto.EventFullDto;
import ru.practicum.explore.event.dto.EventShortDto;
import ru.practicum.explore.event.dto.NewEventDto;
import ru.practicum.explore.event.dto.UpdateEventUserRequest;
import ru.practicum.explore.event.service.EventService;
import ru.practicum.explore.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explore.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explore.request.dto.ParticipationRequestDto;
import ru.practicum.explore.request.service.RequestService;
import static ru.practicum.explore.validation.ValidationGroups.Create;
import static ru.practicum.explore.validation.ValidationGroups.Update;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventPrivateController {
    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @Validated(Create.class) @RequestBody NewEventDto newEventDto) {
        log.info("Получен POST-запрос к эндпоинту: '/users/{userId}/events' на создание события");
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getFullEventById(@PathVariable Long userId,
                                         @PathVariable Long eventId) {
        log.info("Получен GET-запрос к эндпоинту: '/users/{userId}/events/{eventId}' на получение полной информации о событии с ID={}", eventId);
        return eventService.getFullEventById(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getShortEventsByUserId(@PathVariable Long userId,
                                                      @RequestParam(defaultValue = "0") Integer from,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: '/users/{userId}/events' на получение списка всех событий пользователя с ID={}", userId);
        return eventService.getShortEventsByUserId(userId, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Validated(Update.class) @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Получен PATCH-запрос к эндпоинту: '/users/{userId}/events/{eventId}' на обновление события с ID={}", eventId);
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForUsersEvent(@PathVariable long userId,
                                                                  @PathVariable long eventId) {
        log.info("Получен GET-запрос к эндпоинту: '/users/{userId}/events/{eventId}/requests' на получение списка всех " +
                "заявок на участие в событиях пользователя с ID={}", userId);
        return requestService.getRequestsByPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable long userId,
                                                         @PathVariable long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest
                                                                 eventRequestStatusUpdateRequest) {
        log.info("Получен PATCH-запрос к эндпоинту: '/users/{userId}/events/{eventId}/requests' на изменение статуса " +
                "заявки на участие в событии с ID={} пользователя с ID={}", eventId, userId);
        return eventService.updateByPrivate(eventRequestStatusUpdateRequest, userId, eventId);
    }
}
