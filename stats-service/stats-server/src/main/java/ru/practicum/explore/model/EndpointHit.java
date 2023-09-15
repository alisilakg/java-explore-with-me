package ru.practicum.explore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHit {
    private Long id;
    private Long appId;
    private String uri;
    private String ip;
    private Instant timestamp;

}
