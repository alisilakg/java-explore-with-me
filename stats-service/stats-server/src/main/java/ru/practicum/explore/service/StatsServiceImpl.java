package ru.practicum.explore.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.mapper.EndpointHitMapper;
import ru.practicum.explore.mapper.StatsMapper;
import ru.practicum.explore.repository.AppRepository;
import ru.practicum.explore.repository.StatsRepository;
import ru.practicum.explore.model.App;
import ru.practicum.explore.model.EndpointHit;
import ru.practicum.explore.dto.EndpointHitDto;

import javax.transaction.Transactional;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {
    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatsRepository statsRepository;
    private final AppRepository appRepository;

    @Transactional
    @Override
    public void createHit(EndpointHitDto endpointHitDto) {
        String nameApp = endpointHitDto.getApp();
        String uri = endpointHitDto.getUri();
        long appId;

        List<App> apps = appRepository.findByNameAppAndUri(nameApp, uri);

        if (apps.isEmpty()) {
            App appToAdd = new App();
            appToAdd.setName(nameApp);
            appToAdd.setUri(uri);
            appId = appRepository.addApp(appToAdd).getId();
        } else {
            appId = apps.get(0).getId();
        }

        EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(endpointHitDto, appId);
        statsRepository.addEndpointHit(endpointHit);
        log.debug("Пользователь с IP={} просмотрел uri {} сервиса {}", endpointHit.getIp(), uri, nameApp);
    }

    @Override
    public List<ViewStatsDto> getStats(String startEncoded, String endEncoded, List<String> uris, String nameApp) {
        String startDecoded = decodeDateTime(startEncoded);
        String endDecoded = decodeDateTime(endEncoded);

        Instant start = parseDateTime(startDecoded);
        Instant end = parseDateTime(endDecoded);

        if (uris.isEmpty()) {
            Map<Long, Long> stats = statsRepository.getStatsByTime(start, end);
            List<Long> appIds = new ArrayList<>(stats.keySet());
            List<App> apps = new ArrayList<>(appRepository.findAppsByIds(appIds));
            log.debug("Запрос статистики для периода с {} по {}.",
                    start, end);
            return StatsMapper.toViewStats(stats, apps);
        } else {
            List<App> apps = new ArrayList<>(appRepository.findAppsByUris(uris));
            List<Long> appsIds = apps.stream()
                    .map(App::getId)
                    .collect(Collectors.toList());
            Map<Long, Long> stats = statsRepository.getStatsByAppIds(start, end, appsIds);
            log.debug("Запрос статистики для периода с {} по {}. Список uri {}.",
                    start, end, uris);
            return StatsMapper.toViewStats(stats, apps);
        }
    }

    @Override
    public List<ViewStatsDto> getUniqueStats(String startEncoded, String endEncoded, List<String> uris, String nameApp) {
        String startDecoded = decodeDateTime(startEncoded);
        String endDecoded = decodeDateTime(endEncoded);

        Instant start = parseDateTime(startDecoded);
        Instant end = parseDateTime(endDecoded);

        if (uris.isEmpty()) {
            Map<Long, Long> stats = statsRepository.getUniqueStatsByTime(start, end);
            List<Long> appIds = new ArrayList<>(stats.keySet());
            List<App> apps = new ArrayList<>(appRepository.findAppsByIds(appIds));
            log.debug("Запрос уникальной статистики для периода с {} по {}.",
                    start, end);
            return StatsMapper.toViewStats(stats, apps);
        } else {
            List<App> apps = new ArrayList<>(appRepository.findAppsByUris(uris));
            List<Long> appsIds = apps.stream()
                    .map(App::getId)
                    .collect(Collectors.toList());
            Map<Long, Long> stats = statsRepository.getUniqueStatsByAppIds(start, end, appsIds);
            log.debug("Запрос уникальной статистики для периода с {} по {}. Список uri {}.",
                    start, end, uris);
            return StatsMapper.toViewStats(stats, apps);
        }
    }

    private String decodeDateTime(String dateTime) {
        return URLDecoder.decode(dateTime, StandardCharsets.UTF_8);
    }

    private Instant parseDateTime(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
        return localDateTime.toInstant(ZONE_OFFSET);
    }

}
