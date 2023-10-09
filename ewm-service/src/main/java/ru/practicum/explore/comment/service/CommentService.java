package ru.practicum.explore.comment.service;

import ru.practicum.explore.comment.dto.CommentDto;
import ru.practicum.explore.comment.dto.NewCommentDto;
import ru.practicum.explore.comment.dto.UpdateCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto createComment(NewCommentDto newCommentDto, Long userId);

    List<CommentDto> getAllComments(Long eventId,
                                    List<Long> users,
                                    String text,
                                    LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    int from,
                                    int size);

    List<CommentDto> getAllCommentsByUserId(Long userId, Integer from, Integer size);

    CommentDto getCommentById(Long userId, Long commentId);

    CommentDto updateComment(UpdateCommentDto updateCommentDto, Long userId, Long commentId);

    void deleteComment(Long userId, Long commentId);
}