package ru.practicum.explore.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.model.App;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class StatsMapper {

    public static List<ViewStatsDto> toViewStats(Map<Long, Long> stats, List<App> apps) {
        return apps.stream()
                .map(app -> ViewStatsDto.builder()
                        .app(app.getName())
                        .uri(app.getUri())
                        .hits(stats.get(app.getId()))
                        .build())
                .sorted()
                .collect(Collectors.toList());
    }
}
