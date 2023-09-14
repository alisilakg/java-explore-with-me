package ru.practicum.explore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.explore.dto.EndpointHitDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SERVER_URL = "http://stats-server:9090";

    @Autowired
    public StatsClient(RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(SERVER_URL))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postHits(EndpointHitDto endpointHitDto) {
        return post("/hit", endpointHitDto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean uniq, String nameApp) {
        Map<String, Object> params = Map.of(
                "start", encode(start),
                "end", encode(end),
                "uris", String.join(",", uris),
                "unique", uniq,
                "nameApp", nameApp
        );
        return get("/stats?start={start}&end={end}&uris={uris}&uniq={uniq}&nameApp={nameApp}", params);
    }

    private String encode(LocalDateTime dateTime) {
        String dateTimeString = dateTime.format(FORMAT);
        return URLEncoder.encode(dateTimeString, StandardCharsets.UTF_8);
    }
}