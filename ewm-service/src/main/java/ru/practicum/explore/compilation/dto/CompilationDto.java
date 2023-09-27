package ru.practicum.explore.compilation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.explore.event.dto.EventShortDto;

import java.util.List;

@Getter
@Setter
@Builder
public class CompilationDto {
    private long id;
    private Boolean pinned;
    private String title;
    private List<EventShortDto> events;
}
