package ru.practicum.explore.category.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.category.dto.NewCategoryDto;
import ru.practicum.explore.category.mapper.CategoryMapper;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.category.repository.CategoryRepository;
import ru.practicum.explore.error.exception.ConflictException;
import ru.practicum.explore.error.exception.NotFoundException;
import ru.practicum.explore.event.repository.EventRepository;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        categoryRepository.findByName(newCategoryDto.getName()).ifPresent((category) -> {
            throw new ConflictException("Категория с названием " + newCategoryDto.getName() + " уже существует!");
        });
        Category category = CategoryMapper.toCategory(newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long id) {
        getCategoryIfExists(id);
        if (eventRepository.findByCategoryId(id).isPresent()) {
            throw new ConflictException("Категория не может быть удалена, потому что существуют события, связанные с категорией.");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category oldCategory = getCategoryIfExists(catId);
        if (categoryDto.getName().equals(oldCategory.getName())) {
            return CategoryMapper.toCategoryDto(oldCategory);
        }
        categoryRepository.findByName(categoryDto.getName()).ifPresent((category) -> {
            throw new ConflictException("Категория с названием " + categoryDto.getName() + " уже существует!");
        });
        Category newCategory = CategoryMapper.toCategory(categoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(newCategory));
    }

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        Page<Category> categories = categoryRepository.findAll(PageRequest.of(from / size, size));
        return categories.map(CategoryMapper::toCategoryDto).getContent();
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = getCategoryIfExists(catId);
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public Category findCategoryByIdForMapping(Long categoryId) {
        return getCategoryIfExists(categoryId);
    }

    private Category getCategoryIfExists(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + catId + " не найдена"));
    }
}
