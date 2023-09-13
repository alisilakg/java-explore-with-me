package ru.practicum.explore.repository;

import ru.practicum.explore.model.EndpointHit;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface StatsRepository {

    void addEndpointHit(EndpointHit endpointHit);

    Map<Long, Long> getStatsByTime(Instant start, Instant end);

    Map<Long, Long> getStatsByAppIds(Instant start, Instant end, List<Long> appIds);

    Map<Long, Long> getUniqueStatsByTime(Instant start, Instant end);

    Map<Long, Long> getUniqueStatsByAppIds(Instant start, Instant end, List<Long> appIds);
}
