package ru.practicum.explore.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore.comment.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAuthorId(Long userId, Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "WHERE (c.event.id IS NULL OR c.event.id IN :id) " +
            "AND ((:users) IS NULL OR c.author.id IN :users) " +
            "AND LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "AND (c.created BETWEEN coalesce(:rangeStart, c.created) AND coalesce(:rangeEnd, c.created))")
    List<Comment> findAllCommentsByParams(@Param("id") Long id,
                                          @Param("users") List<Long> users,
                                          @Param("text") String text,
                                          @Param("rangeStart") LocalDateTime rangeStart,
                                          @Param("rangeEnd") LocalDateTime rangeEnd,
                                          Pageable pageable);
}

