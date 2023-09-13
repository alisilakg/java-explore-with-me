package ru.practicum.explore.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.model.App;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class AppRepositoryImpl implements AppRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public App addApp(App app) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("apps")
                .usingGeneratedKeyColumns("id");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", app.getName());
        parameters.put("uri", app.getUri());
        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        app.setId(id);
        return app;
    }

    @Override
    public List<App> findByNameAppAndUri(String nameApp, String uri) {
        String sql = "select id, name, uri from apps where name = :nameApp and uri = :uri";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("nameApp", nameApp);
        parameterSource.addValue("uri", uri);
        return namedJdbcTemplate.query(sql, parameterSource, (rs, rowNum) -> mapRowToApp(rs));
    }

    @Override
    public List<App> findAppsByIds(List<Long> ids) {
        String sql = "select id, name, uri from apps where id in (:ids)";
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToApp(rs));
    }

    @Override
    public List<App> findAppsByUris(List<String> uris) {
        String sql = "select id, name, uri from apps where uri in (:uris)";
        SqlParameterSource parameters = new MapSqlParameterSource("uris", uris);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToApp(rs));
    }

    private App mapRowToApp(ResultSet rs) throws SQLException {
        App app = new App();
        app.setId(rs.getLong("id"));
        app.setName(rs.getString("name"));
        app.setUri(rs.getString("uri"));
        return app;
    }
}
