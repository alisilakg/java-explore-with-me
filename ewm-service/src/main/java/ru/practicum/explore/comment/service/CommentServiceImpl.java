package ru.practicum.explore.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.comment.mapper.CommentMapper;
import ru.practicum.explore.comment.dto.CommentDto;
import ru.practicum.explore.comment.dto.NewCommentDto;
import ru.practicum.explore.comment.dto.UpdateCommentDto;
import ru.practicum.explore.comment.model.Comment;
import ru.practicum.explore.comment.repository.CommentRepository;
import ru.practicum.explore.error.exception.NotFoundException;
import ru.practicum.explore.error.exception.ValidationException;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.service.EventService;
import ru.practicum.explore.pagination.CustomPageRequest;
import ru.practicum.explore.user.model.User;
import ru.practicum.explore.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventService eventService;
    private final UserService userService;

    @Override
    @Transactional
    public CommentDto createComment(NewCommentDto newCommentDto, Long userId) {
        User user = userService.findUserByIdForMapping(userId);
        Event event = eventService.findEventByIdForMapping(newCommentDto.getEvent());
        Comment comment = CommentMapper.toComment(newCommentDto, user, event);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
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
        PageRequest pageRequest = CustomPageRequest.of(from, size);
        List<Comment> comments = commentRepository.findAllCommentsByParams(
                eventId,
                users,
                text,
                rangeStart,
                rangeEnd,
                pageRequest);

        return CommentMapper.toCommentDto(getCommentsWithEventWithViewsAndRequests(comments));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentsByUserId(Long userId, Integer from, Integer size) {
        userService.findUserByIdForMapping(userId);
        PageRequest pageRequest = CustomPageRequest.of(from, size);
        List<Comment> comments = commentRepository.findByAuthorId(userId, pageRequest);
        List<Comment> commentsWithEventsDto = getCommentsWithEventWithViewsAndRequests(comments);

        return commentsWithEventsDto.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(Long userId, Long commentId) {
        userService.findUserByIdForMapping(userId);
        Comment comment = getCommentWithEventWithViewsAndRequests(commentId);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(UpdateCommentDto updateCommentDto, Long userId, Long commentId) {
        userService.findUserByIdForMapping(userId);
        Comment comment = getCommentWithEventWithViewsAndRequests(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ValidationException("Изменить комментарий может только его автор.");
        }
        if (Objects.nonNull(updateCommentDto.getText()) && !updateCommentDto.getText().isBlank()) {
            comment.setText(updateCommentDto.getText());
        }

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        User user = userService.findUserByIdForMapping(userId);
        if (comment.getAuthor().getId().equals(user.getId())) {
            commentRepository.deleteById(commentId);
        } else {
            throw new ValidationException("Удалить комментарий может только его автор.");
        }
    }

    private Comment getCommentIfExists(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментарий с id = " + commentId + " не найден."));
    }

    private Comment getCommentWithEventWithViewsAndRequests(Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        Event event = comment.getEvent();
        List<Event> eventsWithViewsAndRequests = eventService.getEventsWithViewsAndCountRequests(List.of(event));
        comment.setEvent(eventsWithViewsAndRequests.get(0));
        return comment;
    }

    private List<Comment> getCommentsWithEventWithViewsAndRequests(List<Comment> comments) {
        List<Event> events = comments.stream().map(Comment::getEvent).collect(Collectors.toList());
        List<Event> eventsWithViewsAndRequests = eventService.getEventsWithViewsAndCountRequests(events);
        for (Comment comment : comments) {
            Long commentId = comment.getEvent().getId();
            Event event = eventsWithViewsAndRequests.stream().filter(event1 -> commentId.equals(event1.getId())).findFirst().orElse(null);
            comment.setEvent(event);
        }
        return comments;
    }

}
