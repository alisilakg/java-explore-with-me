package ru.practicum.explore.comment.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.explore.comment.dto.CommentDto;
import ru.practicum.explore.comment.dto.NewCommentDto;
import ru.practicum.explore.comment.model.Comment;
import ru.practicum.explore.event.mapper.EventMapper;
import ru.practicum.explore.event.service.EventService;
import ru.practicum.explore.user.mapper.UserMapper;
import ru.practicum.explore.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommentMapper {
    private final EventService eventService;
    private final UserService userService;
    private final EventMapper eventMapper;

    @Autowired
    public CommentMapper(EventService eventService, UserService userService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.userService = userService;
        this.eventMapper = eventMapper;
    }

    public Comment toComment(NewCommentDto newCommentDto, Long userId) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .event(eventService.findEventByIdForMapping(newCommentDto.getEvent()))
                .author(userService.findUserByIdForMapping(userId))
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .event(eventMapper.toEventShortDto(comment.getEvent()))
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .created(comment.getCreated())
                .build();
    }

    public List<CommentDto> toCommentDto(Iterable<Comment> comments) {
        List<CommentDto> result = new ArrayList<>();

        for (Comment comment : comments) {
            result.add(toCommentDto(comment));
        }

        return result;
    }
}
