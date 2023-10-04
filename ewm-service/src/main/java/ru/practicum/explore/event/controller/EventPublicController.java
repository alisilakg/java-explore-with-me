package ru.practicum.explore.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.enums.EventSort;
import ru.practicum.explore.event.dto.EventFullDto;
import ru.practicum.explore.event.dto.EventShortDto;
import ru.practicum.explore.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Slf4j
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllPublishedEvents(@RequestParam(required = false) String text,
                                                     @RequestParam(required = false) List<Long> categories,
                                                     @RequestParam(required = false) Boolean paid,
                                                     @RequestParam(required = false)
                                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                         LocalDateTime rangeStart,
                                                     @RequestParam(required = false)
                                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                         LocalDateTime rangeEnd,
                                                     @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                     @RequestParam(defaultValue = "VIEWS") EventSort sort,
                                                     @RequestParam(defaultValue = "0") Integer from,
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
