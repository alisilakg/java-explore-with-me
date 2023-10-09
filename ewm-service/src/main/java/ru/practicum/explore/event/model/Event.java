package ru.practicum.explore.event.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.explore.category.model.Category;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.location.model.Location;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_annotation", nullable = false, length = 2000)
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @CreationTimestamp
    private LocalDateTime createdOn;
    @Column(name = "event_description", nullable = false, length = 7000)
    private String description;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    @Column(name = "paid", nullable = false)
    private Boolean paid;
    @Column(name = "participant_limit", nullable = false)
    private Long participantLimit;
    @Column(name = "published_on", nullable = false)
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    private EventState state;
    @Column(name = "event_title", nullable = false, length = 120)
    private String title;
    @Transient
    private Long views;
    @Transient
    private Long confirmedRequests;
}

