package ru.practicum.explore.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.event.model.Event;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, CustomEventRepository {
    List<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE ((:users) IS NULL OR e.initiator.id IN :users) " +
            "AND ((:states) IS NULL OR e.state IN :states) " +
            "AND ((:categories) IS NULL OR e.category.id IN :categories) " +
            "AND (e.eventDate BETWEEN coalesce(:rangeStart, e.eventDate) AND coalesce(:rangeEnd, e.eventDate))")
    List<Event> findAllEventsByAdmin(@Param("users") List<Long> users,
                                     @Param("states") List<EventState> states,
                                     @Param("categories") List<Long> categories,
                                     @Param("rangeStart") LocalDateTime rangeStart,
                                     @Param("rangeEnd") LocalDateTime rangeEnd,
                                     Pageable pageable);

    Optional<Event> findByCategoryId(Long id);

}

