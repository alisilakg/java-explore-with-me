package ru.practicum.explore.comment.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "text", nullable = false, length = 1024)
    private String text;
    @ManyToOne()
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @ManyToOne()
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @CreationTimestamp
    private LocalDateTime created;
}
