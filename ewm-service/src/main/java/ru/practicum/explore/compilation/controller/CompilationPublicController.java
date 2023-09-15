package ru.practicum.explore.compilation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@Slf4j
public class CompilationPublicController {
    private final CompilationService compilationService;

    @Autowired
    public CompilationPublicController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @RequestParam(required = false, defaultValue = "0") Integer from,
                                                   @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: '/compilations' на получение подборок событий.");

        return compilationService.getAllCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("Получен GET-запрос к эндпоинту: '/compilations/{compId}' на получение подборки событий с ID={}", compId);
        return compilationService.getCompilationById(compId);
    }

}