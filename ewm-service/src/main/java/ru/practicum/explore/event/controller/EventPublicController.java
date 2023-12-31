package ru.practicum.explore.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.enums.EventSort;
import ru.practicum.explore.event.dto.EventFullDto;
import ru.practicum.explore.event.dto.EventShortDto;
import ru.practicum.explore.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventPublicController {
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllPublishedEvents(@RequestParam(required = false) String text,
                                                     @RequestParam(required = false) List<Long> categories,
                                                     @RequestParam(required = false) Boolean paid,
                                                     @RequestParam(required = false)
                                                         @DateTimeFormat(pattern = TIME_FORMAT)
                                                         LocalDateTime rangeStart,
                                                     @RequestParam(required = false)
                                                         @DateTimeFormat(pattern = TIME_FORMAT)
                                                         LocalDateTime rangeEnd,
                                                     @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                     @RequestParam(defaultValue = "VIEWS") EventSort sort,
                                                     @PositiveOrZero
                                                     @RequestParam(defaultValue = "0") Integer from,
                                                     @Positive
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     HttpServletRequest request) {
        log.info("Получен GET-запрос к эндпоинту: '/events' на получение краткой информации о событиях.");
        String ip = request.getRemoteAddr();
        String url = request.getRequestURI();
        return eventService.getAllPublishedEvents(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size,
                ip,
                url);
    }

    @GetMapping("/{id}")
    public EventFullDto getPublishedEventById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Получен GET-запрос к эндпоинту: '/events/{eventId}' на получение полной информации об опубликованном событии с ID={}", id);
        String ip = request.getRemoteAddr();
        String url = request.getRequestURI();
        return eventService.getPublishedEventById(id, ip, url);
    }
}
