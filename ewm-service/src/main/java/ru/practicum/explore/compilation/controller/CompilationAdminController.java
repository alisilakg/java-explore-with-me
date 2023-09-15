package ru.practicum.explore.compilation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.NewCompilationDto;
import ru.practicum.explore.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explore.compilation.service.CompilationService;

import static ru.practicum.explore.validation.ValidationGroups.Create;
import static ru.practicum.explore.validation.ValidationGroups.Update;
@RestController
@RequestMapping(path = "/admin/compilations")
@Validated
@Slf4j
public class CompilationAdminController {
    private final CompilationService compilationService;

    @Autowired
    public CompilationAdminController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Validated(Create.class) @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Получен POST-запрос к эндпоинту: '/admin/compilations' на создание подборкт событий.");
        return compilationService.createCompilation(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/admin/compilations/{compId}' на удаление подборки событий с ID={}", compId);
        compilationService.deleteCompilationById(compId);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Validated(Update.class) @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.info("Получен PATCH-запрос к эндпоинту: '/admin/compilations/{compId}' на обновление подборки событий с ID={}", compId);
        return compilationService.updateCompilation(compId, updateCompilationRequest);
    }

}
