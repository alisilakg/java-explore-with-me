package ru.practicum.explore.repository;

import ru.practicum.explore.model.App;

import java.util.List;

public interface AppRepository {
    App addApp(App app);

    List<App> findByNameAppAndUri(String nameApp, String uri);

    List<App> findAppsByIds(List<Long> ids);

    List<App> findAppsByUris(List<String> uris);
}
