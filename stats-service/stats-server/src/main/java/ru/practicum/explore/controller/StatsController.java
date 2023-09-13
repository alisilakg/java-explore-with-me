package ru.practicum.explore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.service.StatsService;
import ru.practicum.explore.dto.EndpointHitDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createHit(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("Получен POST-запрос к эндпоинту: '/hit'");
        statsService.createHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@RequestParam String start,
                                                       @RequestParam String end,
                                                       @RequestParam(required = false, defaultValue = "") List<String> uris,
                                                       @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Получен GET-запрос к эндпоинту: '/stats' на получение статистики" +
                " с параметрами start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        if (unique) {
            return ResponseEntity.ok(statsService.getUniqueStats(start, end, uris));
        } else {
            return ResponseEntity.ok(statsService.getStats(start, end, uris));
        }
    }

}
