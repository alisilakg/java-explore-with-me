package ru.practicum.explore.repository;

import ru.practicum.explore.model.App;
import ru.practicum.explore.model.EndpointHit;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface StatsRepository {

    void addEndpointHit(EndpointHit endpointHit);

    Map<String, Long> getStatsByTime(Instant start, Instant end);

    Map<String, Long> getUniqueStatsByTime(Instant start, Instant end);

    Map<String, Long> getUniqueStatsByUris(Instant start, Instant end, List<String> uris);

    App addApp(App app);

    App findAppById(Long id);

    Long findIdByNameApp(String nameApp);

    Map<String, Long> getStatsByUris(Instant start, Instant end, List<String> uris);
}
