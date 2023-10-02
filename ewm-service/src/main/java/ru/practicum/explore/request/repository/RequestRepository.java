package ru.practicum.explore.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore.enums.RequestStatus;
import ru.practicum.explore.request.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long userId);

    Long countByEventIdAndStatus(Long id, RequestStatus requestStatus);

    List<Request> findByRequesterId(Long userId);

    List<Request> findByEventId(Long eventId);

    @Query("select COUNT(r) FROM Request r where r.event.id = :eventId AND r.status = :status")
    Long getCountByEventIdAndState(@Param("eventId") Long eventId, @Param("status") RequestStatus status);

    List<Request> findAllByIdIn(List<Long> ids);

}
