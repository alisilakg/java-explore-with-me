package ru.practicum.explore.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
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
        parameters.put("timestamp", Timestamp.from(endpointHit.getTimestamp()));
        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        endpointHit.setId(id);
    }

    @Override
    public Map<Long, Long> getStatsByTime(Instant start, Instant end) {
        String sql = "select app_id, COUNT(ip) as views from hits " +
                "where (timestamp between :start and :end) " +
                "group by app_id";
        MapSqlParameterSource parameterSource = getParamsWithDates(start, end);
        return makeQuery(sql, parameterSource);
    }

    @Override
    public Map<Long, Long> getStatsByAppIds(Instant start, Instant end, List<Long> appIds) {
        String sql = "select app_id, COUNT(ip) as views from hits " +
                "where (timestamp between :start and :end) " +
                "AND app_id in (:appIds) " +
                "group by app_id";
        MapSqlParameterSource parameterSource = getParamsWithDates(start, end);
        parameterSource.addValue("appIds", appIds);
        return makeQuery(sql, parameterSource);
    }

    @Override
    public Map<Long, Long> getUniqueStatsByTime(Instant start, Instant end) {
        String sql = "select app_id, COUNT(DISTINCT(ip)) as views from hits " +
                "where (timestamp between :start and :end) " +
                "group by app_id";
        MapSqlParameterSource parameterSource = getParamsWithDates(start, end);
        return makeQuery(sql, parameterSource);
    }

    @Override
    public Map<Long, Long> getUniqueStatsByAppIds(Instant start, Instant end, List<Long> appIds) {
        String sql = "select app_id, COUNT(DISTINCT(ip)) as views from hits " +
                "where (timestamp between :start and :end) " +
                "AND app_id in (:appIds) " +
                "group by app_id";
        MapSqlParameterSource parameterSource = getParamsWithDates(start, end);
        parameterSource.addValue("appIds", appIds);
        return makeQuery(sql, parameterSource);
    }

    private MapSqlParameterSource getParamsWithDates(Instant start, Instant end) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", Timestamp.from(start));
        parameters.addValue("end", Timestamp.from(end));
        return parameters;
    }

    private Map<Long, Long> makeQuery(String sql, MapSqlParameterSource parameters) {
        final Map<Long, Long> result = new HashMap<>();
        namedJdbcTemplate.query(sql, parameters,
                rs -> {
                    long appId = rs.getLong("app_id");
                    long views = rs.getLong("views");
                    result.put(appId, views);
                });
        return result;
    }
}
