package ru.practicum.explore.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @PositiveOrZero
                                                   @RequestParam(defaultValue = "0") Integer from,
                                                   @Positive
                                                   @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: '/compilations' на получение подборок событий.");

        return compilationService.getAllCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("Получен GET-запрос к эндпоинту: '/compilations/{compId}' на получение подборки событий с ID={}", compId);
        return compilationService.getCompilationById(compId);
    }

}
