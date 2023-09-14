package ru.practicum.explore.service;

import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.ViewStatsDto;

import java.util.List;

public interface StatsService {
    void createHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(String start, String end, List<String> uris, String nameApp);

    List<ViewStatsDto> getUniqueStats(String start, String end, List<String> uris, String nameApp);
}
