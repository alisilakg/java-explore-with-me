package ru.practicum.explore.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.model.App;
import ru.practicum.explore.model.EndpointHit;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class StatsRepositoryImpl implements StatsRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public void addEndpointHit(EndpointHit endpointHit) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("hits")
                .usingGeneratedKeyColumns("id");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("app_id", endpointHit.getAppId());
        parameters.put("ip", endpointHit.getIp());
        parameters.put("uri", endpointHit.getUri());
        parameters.put("timestamp", Timestamp.from(endpointHit.getTimestamp()));
        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        endpointHit.setId(id);
    }

    @Override
    public Map<String, Long> getStatsByTime(Instant start, Instant end) {
        String sql = "select uri, COUNT(ip) as views from hits " +
                "where (timestamp between :start and :end) " +
                "group by app_id, uri " +
                "order by views DESC ";
        MapSqlParameterSource parameterSource = getParamsWithDates(start, end);
        return makeQuery(sql, parameterSource);
    }

    @Override
    public Map<String, Long> getStatsByUris(Instant start, Instant end, List<String> uris) {
        String sql = "select uri, COUNT(ip) as views from hits " +
                "where (timestamp between :start and :end) " +
                "AND uri in (:uris) " +
                "group by app_id, uri " +
                "order by views DESC ";
        MapSqlParameterSource parameterSource = getParamsWithDates(start, end);
        parameterSource.addValue("uris", uris);
        return makeQuery(sql, parameterSource);
    }

    @Override
    public Map<String, Long> getUniqueStatsByTime(Instant start, Instant end) {
        String sql = "select uri, COUNT(DISTINCT(ip)) as views from hits " +
                "where (timestamp between :start and :end) " +
                "group by app_id, uri " +
                "order by views DESC ";
        MapSqlParameterSource parameterSource = getParamsWithDates(start, end);
        return makeQuery(sql, parameterSource);
    }

    @Override
    public Map<String, Long> getUniqueStatsByUris(Instant start, Instant end, List<String> uris) {
        String sql = "select uri, COUNT(DISTINCT(ip)) as views from hits " +
                "where (timestamp between :start and :end) " +
                "AND uri in (:uris) " +
                "group by app_id, uri " +
                "order by views DESC ";
        MapSqlParameterSource parameterSource = getParamsWithDates(start, end);
        parameterSource.addValue("uris", uris);
        return makeQuery(sql, parameterSource);
    }

    private MapSqlParameterSource getParamsWithDates(Instant start, Instant end) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", Timestamp.from(start));
        parameters.addValue("end", Timestamp.from(end));
        return parameters;
    }

    private Map<String, Long> makeQuery(String sql, MapSqlParameterSource parameters) {
        final Map<String, Long> result = new HashMap<>();
        namedJdbcTemplate.query(sql, parameters,
                rs -> {
                    String uri = rs.getString("uri");
                    long views = rs.getLong("views");
                    result.put(uri, views);
                });
        return result;
    }

    @Override
    public App addApp(App app) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("apps")
                .usingGeneratedKeyColumns("id");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", app.getName());
        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        app.setId(id);
        return app;
    }

    @Override
    public App findAppById(Long id) {
        String sql = "select id, name from apps where id = :id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("id", id);
        return namedJdbcTemplate.queryForObject(sql, namedParameters, appRowMapper);
    }

    @Override
    public Long findIdByNameApp(String nameApp) {
        String sql = "select id from apps where name = :nameApp";
        SqlParameterSource namedParameters = new MapSqlParameterSource("nameApp", nameApp);
        return namedJdbcTemplate.queryForObject(sql, namedParameters, Long.class);
    }

    private final RowMapper<App> appRowMapper = (rs, rowNum) -> {
        App app = new App();
        app.setId(rs.getLong("id"));
        app.setName(rs.getString("name"));
        return app;
    };
}
