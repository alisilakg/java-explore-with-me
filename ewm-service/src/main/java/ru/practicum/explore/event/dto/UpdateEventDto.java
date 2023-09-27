package ru.practicum.explore.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import ru.practicum.explore.location.model.Location;

import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UpdateEventDto {
    //Данные для изменения информации о событии.
    // Если поле в запросе не указано (равно null) - значит изменение этих данных не треубется.
    @Length(min = 20, max = 2000, message = "Размер аннотации должен быть от 20 до 2000 символов")
    protected String annotation;//Новая аннотация
    protected Long category;//Новая категория
    @Length(min = 20, max = 7000, message = "Размер описания должен быть от 20 до 7000 символов")
    protected String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @FutureOrPresent(message = "Поле должно содержать дату, которая еще не наступила")
    protected LocalDateTime eventDate;
    protected Location location;
    protected Boolean paid;
    protected Long participantLimit;
    protected Boolean requestModeration;
    @Length(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    protected String title;
}

