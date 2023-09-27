package ru.practicum.explore.compilation.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NewCompilationDto {
    private Boolean pinned;
    @NotBlank(message = "Название подборки не может быть пустым")
    @Length(min = 1, max = 50, message = "Название подборки должно быть от 1 до 50 символов")
    private String title;
    private List<Long> events = new ArrayList<>();
}
