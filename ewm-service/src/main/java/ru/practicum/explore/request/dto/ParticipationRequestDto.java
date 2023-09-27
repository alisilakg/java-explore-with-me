package ru.practicum.explore.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.explore.enums.RequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ParticipationRequestDto {
    //заявка на участие в событии
    private long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private long event;
    private long requester;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private RequestStatus status;
}