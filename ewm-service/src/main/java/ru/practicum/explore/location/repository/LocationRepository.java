package ru.practicum.explore.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explore.location.model.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> getByLatAndLon(Float lat, Float lon);
}
