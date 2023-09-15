package ru.practicum.explore;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.explore.dto.EndpointHitDto;
import ru.practicum.explore.dto.ViewStatsDto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatsClient extends BaseClient {

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(serverUrl, builder);
    }

    public ViewStatsDto postHits(EndpointHitDto endpointHitDto) {
        Gson gson = new Gson();

        ResponseEntity<Object> objectResponseEntity = post("/hit", endpointHitDto);
        String json = gson.toJson(objectResponseEntity.getBody());
        return gson.fromJson(json, ViewStatsDto.class);
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique, String nameApp) {
        Gson gson = new Gson();
        Map<String, Object> parameters = Map.of(
                "uris", String.join(",", uris),
                "unique", unique,
                "start", start,
                "end", end,
                "nameApp", nameApp
        );
        ResponseEntity<Object> objectResponseEntity =
                get("/stats?start={start}&end={end}&uris={uris}&unique={unique}&nameApp={nameApp}", parameters);
        String json = gson.toJson(objectResponseEntity.getBody());
        ViewStatsDto[] viewStatDtoArray = gson.fromJson(json, ViewStatsDto[].class);

        return Arrays.asList(viewStatDtoArray);
    }
}