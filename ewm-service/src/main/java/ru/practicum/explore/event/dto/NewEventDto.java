package ru.practicum.explore.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.practicum.explore.location.model.Location;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
public class NewEventDto {
    @NotBlank(message = "Отсутствует текст в аннотации")
    @Length(min = 20, max = 2000, message = "Размер аннотации должен быть от 20 до 2000 символов")
    private String annotation;//Краткое описание события
    @NotNull
    private Long category;
    @NotBlank(message = "Отсутствует текст в описании")
    @Length(min = 20, max = 7000, message = "Размер описания должен быть от 20 до 7000 символов")
    private String description;
    @NotNull
    @FutureOrPresent(message = "Поле должно содержать дату, которая еще не наступила")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull
    private Location location;
    private Boolean paid; //default: false; Нужно ли оплачивать участие в событии
    private Long participantLimit;//default: 0; Ограничение на количество участников. Значение 0 - означает отсутствие ограничения
    private Boolean requestModeration;//default: true; Нужна ли пре-модерация заявок на участие.
    // Если true, то все заявки будут ожидать подтверждения инициатором события.
    // Если false - то будут подтверждаться автоматически.
    @NotBlank(message = "Отсутствует текст в заголовке")
    @Length(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    private String title;
}
