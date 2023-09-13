package ru.practicum.explore.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.model.EndpointHit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@UtilityClass
public class EndpointHitMapper {
    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public EndpointHit toEndpointHit(EndpointHitDto endpointHitDto, Long appId) {
        Instant timestamp = toInstant(endpointHitDto.getTimestamp());

        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setId(endpointHitDto.getId());
        endpointHit.setAppId(appId);
        endpointHit.setUri(endpointHitDto.getUri());
        endpointHit.setIp(endpointHitDto.getIp());
        endpointHit.setTimestamp(timestamp);
        return endpointHit;
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZONE_OFFSET);
    }
}
