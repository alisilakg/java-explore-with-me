package ru.practicum.explore.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
public class EventRequestStatusUpdateRequest {
    @NotBlank(message = "Отсутствует новый статус заявок")
    private String status;
    @NotNull(message = "Отсутствует список заявок")
    private List<Long> requestIds;
}