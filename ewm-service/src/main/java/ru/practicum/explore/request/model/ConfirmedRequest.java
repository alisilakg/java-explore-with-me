package ru.practicum.explore.request.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmedRequest {
    private Long count;
    private Long eventId;

    public ConfirmedRequest(Long eventId, Long count) {
        this.eventId = eventId;
        this.count = count;

    }
}
