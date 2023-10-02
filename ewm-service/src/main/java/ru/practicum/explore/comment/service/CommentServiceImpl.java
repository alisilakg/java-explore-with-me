package ru.practicum.explore.comment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explore.comment.mapper.CommentMapper;
import ru.practicum.explore.comment.dto.CommentDto;
import ru.practicum.explore.comment.dto.NewCommentDto;
import ru.practicum.explore.comment.dto.UpdateCommentDto;
import ru.practicum.explore.comment.model.Comment;
import ru.practicum.explore.comment.repository.CommentRepository;
import ru.practicum.explore.error.exception.NotFoundException;
import ru.practicum.explore.error.exception.ValidationException;
import ru.practicum.explore.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, CommentMapper commentMapper,
                              UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.userRepository = userRepository;
    }

    @Override
    public CommentDto createComment(NewCommentDto newCommentDto, Long userId) {
        checkUser(userId);
        Comment comment = commentMapper.toComment(newCommentDto, userId);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getAllComments(Long eventId, List<Long> users, String text, LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd, int from, int size) {
        if (rangeStart != null || rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new ValidationException("Время окончания не должно быть позднее времени начала.");
            }
        }
        if (users != null && users.size() == 1 && users.get(0).equals(0L)) {
            users = null;
        }

        List<Comment> comments = commentRepository.findAllCommentsByParams(
                eventId,
                users,
                text,
                rangeStart,
                rangeEnd,
                PageRequest.of(from, size));

        return commentMapper.toCommentDto(comments);
    }

    @Override
    public List<CommentDto> getAllCommentsByUserId(Long userId, Integer from, Integer size) {
        checkUser(userId);
        int page = from / size;

        List<Comment> comments = commentRepository.findByAuthorId(userId, PageRequest.of(page, size));
        return comments.stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getCommentById(Long userId, Long commentId) {
        checkUser(userId);
        Comment comment = getCommentIfExists(commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public CommentDto updateComment(UpdateCommentDto updateCommentDto, Long userId, Long commentId) {
        checkUser(userId);
        Comment comment = getCommentIfExists(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ValidationException("Изменить комментарий может только его автор.");
        }
        if (Objects.nonNull(updateCommentDto.getText()) && !updateCommentDto.getText().isBlank()) {
            comment.setText(updateCommentDto.getText());
        }
        commentRepository.save(comment);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        getCommentIfExists(commentId);
        checkUser(userId);
        List<Comment> comments = commentRepository.findByAuthorId(userId, PageRequest.of(0, 10));
        if (comments.stream().anyMatch(comment -> comment.getId().equals(commentId))) {
            commentRepository.deleteById(commentId);
        } else {
            throw new ValidationException("Удалить комментарий может только его автор.");
        }

    }

    private Comment getCommentIfExists(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментарий с id = " + commentId + " не найден."));
    }

    private void checkUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не зарегистрирован."));
    }
}
