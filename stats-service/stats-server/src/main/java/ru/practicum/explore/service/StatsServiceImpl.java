package ru.practicum.explore.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.ViewStatsDto;
import ru.practicum.explore.mapper.EndpointHitMapper;
import ru.practicum.explore.mapper.StatsMapper;
import ru.practicum.explore.repository.StatsRepository;
import ru.practicum.explore.model.App;
import ru.practicum.explore.model.EndpointHit;
import ru.practicum.explore.dto.EndpointHitDto;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.transaction.Transactional;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {
    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatsRepository statsRepository;


    @Transactional
    @Override
    public void createHit(EndpointHitDto endpointHitDto) {
        String nameApp = endpointHitDto.getApp();
        try {
            Long id = statsRepository.findIdByNameApp(nameApp);
            EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(endpointHitDto, id);
            statsRepository.addEndpointHit(endpointHit);
            log.debug("Пользователь с IP={} просмотрел uri {} сервиса {}", endpointHit.getIp(), endpointHitDto.getUri(), nameApp);
        } catch (EmptyResultDataAccessException e) {
            App appToAdd = App.builder()
                    .name(nameApp)
                    .build();
            App app = statsRepository.addApp(appToAdd);
            EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(endpointHitDto, app.getId());
            statsRepository.addEndpointHit(endpointHit);
            log.debug("Пользователь с IP={} просмотрел uri {} сервиса {}", endpointHit.getIp(), endpointHitDto.getUri(), nameApp);
        }
    }

    @Override
    public List<ViewStatsDto> getStats(String startEncoded, String endEncoded, List<String> uris, String nameApp) {
        String startDecoded = decodeDateTime(startEncoded);
        String endDecoded = decodeDateTime(endEncoded);

        Instant start = parseDateTime(startDecoded);
        Instant end = parseDateTime(endDecoded);
        App app = findAppByName(nameApp);

        Map<String, Long> stats;
        if (uris.isEmpty()) {
            stats = statsRepository.getStatsByTime(start, end);
            log.debug("Запрос статистики сервиса с id={} для периода с {} по {}.",
                    app.getId(), start, end);
        } else {
            stats = statsRepository.getStatsByUris(start, end, uris);
            log.debug("Запрос статистики сервиса с id={} для периода с {} по {}. Список uri {}.",
                    app.getId(), start, end, uris);
        }
        return StatsMapper.toViewStats(stats, app);
    }

    @Override
    public List<ViewStatsDto> getUniqueStats(String startEncoded, String endEncoded, List<String> uris, String nameApp) {
        String startDecoded = decodeDateTime(startEncoded);
        String endDecoded = decodeDateTime(endEncoded);

        Instant start = parseDateTime(startDecoded);
        Instant end = parseDateTime(endDecoded);

        App app = findAppByName(nameApp);

        Map<String, Long> stats;
        if (uris.isEmpty()) {
            stats = statsRepository.getUniqueStatsByTime(start, end);
            log.debug("Запрос уникальной статистики сервиса с id={} для периода с {} по {}.",
                    app.getId(), start, end);
        } else {
            stats = statsRepository.getUniqueStatsByUris(start, end, uris);
            log.debug("Запрос уникальной статистики сервиса с id={} для периода с {} по {}. Список uri {}.",
                    app.getId(), start, end, uris);
        }
        return StatsMapper.toViewStats(stats, app);
    }

    private App findAppByName(String nameApp) {
        Long id = statsRepository.findIdByNameApp(nameApp);
        return statsRepository.findAppById(id);
    }

    private String decodeDateTime(String dateTime) {
        return URLDecoder.decode(dateTime, StandardCharsets.UTF_8);
    }

    private Instant parseDateTime(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
        return localDateTime.toInstant(ZONE_OFFSET);
    }

}
