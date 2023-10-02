package ru.practicum.explore.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.enums.EventSort;
import ru.practicum.explore.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto implements Comparable<EventShortDto> {
    private Long id;
    private String annotation;//Краткое описание
    private CategoryDto category;
    private long confirmedRequests;//Количество одобренных заявок на участие в данном событии
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;//Дата и время на которые намечено событие (в формате "yyyy-MM-dd HH:mm:ss")
    private UserShortDto initiator;
    private Boolean paid;//Нужно ли оплачивать участие
    private String title;//Заголовок
    private long views;//Количество просмотрев события

    @Override
    public int compareTo(EventShortDto other) {
        return this.id.compareTo(other.id);
    }

    public static final Comparator<EventShortDto> EVENT_DATE_COMPARATOR =
            Comparator.comparing((EventShortDto::getEventDate))
                    .thenComparing(EventShortDto::getId);

    public static final Comparator<EventShortDto> VIEWS_COMPARATOR =
            Comparator.comparing(EventShortDto::getViews)
                    .thenComparing(EventShortDto::getId);

    public static Comparator<EventShortDto> getComparator(EventSort sortType) {
        if (Objects.nonNull(sortType) && sortType == EventSort.VIEWS) {
            return VIEWS_COMPARATOR.reversed();
        }
        return EVENT_DATE_COMPARATOR.reversed();
    }

}
