package ru.practicum.explore.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.model.App;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class StatsMapper {

    public static List<ViewStatsDto> toViewStats(Map<String, Long> stats, App app) {
        List<ViewStatsDto> list = new ArrayList<>();
        for (Map.Entry<String, Long> stat : stats.entrySet()) {
            ViewStatsDto viewStatsDto = ViewStatsDto.builder()
                    .app(app.getName())
                    .uri(stat.getKey())
                    .hits(stat.getValue())
                    .build();
            list.add(viewStatsDto);
        }
        return list.stream()
                .sorted()
                .collect(Collectors.toList());
    }
}