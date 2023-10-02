package ru.practicum.explore.comment.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class NewCommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    @NotEmpty(message = "Текст комментария не может быть пустым")
    @Length(min = 1, max = 1024, message = "Длина текста комментария должна быть от 1 до 1024 символов")
    private String text;
    @NotNull
    private Long event;
}
