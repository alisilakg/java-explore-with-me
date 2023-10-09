package ru.practicum.explore.comment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.explore.event.dto.EventShortDto;
import ru.practicum.explore.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private EventShortDto event;
    private UserShortDto author;
    private LocalDateTime created;
}
