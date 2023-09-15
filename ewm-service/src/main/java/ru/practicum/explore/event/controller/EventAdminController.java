package ru.practicum.explore.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.event.dto.EventFullDto;
import ru.practicum.explore.event.dto.UpdateEventAdminRequest;
import ru.practicum.explore.event.service.EventService;

import static ru.practicum.explore.validation.ValidationGroups.Update;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@Validated
@Slf4j
public class EventAdminController {
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final EventService eventService;

    @Autowired
    public EventAdminController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventFullDto> getAllByCriteriaForAdmin(@RequestParam(required = false) List<Long> users,
                                     @RequestParam(required = false) List<EventState> states,
                                     @RequestParam(required = false) List<Long> categories,
                                     @RequestParam(required = false, name = "rangeStart")
                                                           @DateTimeFormat(pattern = TIME_FORMAT) LocalDateTime start,
                                     @RequestParam(required = false, name = "rangeEnd")
                                                           @DateTimeFormat(pattern = TIME_FORMAT) LocalDateTime end,
                                     @RequestParam(defaultValue = "0") Integer from,
                                     @RequestParam(defaultValue = "10") Integer size) {
        if (states != null) {
            states.forEach(stateParam -> EventState.from(stateParam.toString())
                    .orElseThrow(() -> new IllegalArgumentException("Неизвестный статус события: " + stateParam)));
        }
        log.info("Получен GET-запрос к эндпоинту: '/admin/events' на получение полной информации обо всех событиях, " +
                "подходящих под переданные условия.");
        return eventService.getAllEventsByAdmin(users, states, categories, start, end, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto patchEventByAdmin(@PathVariable long eventId,
                                          @Validated(Update.class) @RequestBody UpdateEventAdminRequest updateEventDto) {
        log.info("Получен PATCH-запрос к эндпоинту: '/admin/events/{eventId}' на редактирование данных любого события администратором.");
        return eventService.patchEventByAdmin(eventId, updateEventDto);
    }
}