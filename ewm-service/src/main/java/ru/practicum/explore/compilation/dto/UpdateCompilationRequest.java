package ru.practicum.explore.compilation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class UpdateCompilationRequest {
    private Boolean pinned;
    @Length(min = 1, max = 50, message = "Название подборки должно быть от 1 до 50 символов")
    private String title;
    private final List<Long> events = new ArrayList<>();
}
