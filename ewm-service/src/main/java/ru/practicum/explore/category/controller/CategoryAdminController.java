package ru.practicum.explore.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.category.dto.NewCategoryDto;
import ru.practicum.explore.category.service.CategoryService;

import static ru.practicum.explore.validation.ValidationGroups.Create;
import static ru.practicum.explore.validation.ValidationGroups.Update;

@RestController
@RequestMapping(path = "/admin/categories")
@Validated
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Validated(Create.class) @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Получен POST-запрос к эндпоинту: '/admin/categories' на создание категории");
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        log.info("Получен DELETE-запрос к эндпоинту: '/admin/category/{id}' на удаление категории с ID={}", id);
        categoryService.deleteCategoryById(id);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@Validated(Update.class) @RequestBody CategoryDto categoryDto,
                                      @PathVariable Long catId) {
        log.info("Получен PATCH-запрос к эндпоинту: '/admin/categories' на обновление категории с ID={}", catId);
        return categoryService.updateCategory(catId, categoryDto);
    }
}
