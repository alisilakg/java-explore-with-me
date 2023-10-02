package ru.practicum.explore.event.repository;

import ru.practicum.explore.event.dto.EventFilterParams;
import ru.practicum.explore.event.model.Event;

import java.util.List;

public interface CustomEventRepository {
    List<Event> findAllEventsByParams(EventFilterParams params);
}
