package ru.practicum.explore.category.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
public class CategoryDto {
    private long id;
    @NotBlank(message = "Название категории не может быть пустым")
    @NotEmpty(message = "Название категории не может быть пустым")
    @Length(min = 1, max = 50, message = "Размер названия должен быть от 1 до 50 символов")
    private String name;
}
