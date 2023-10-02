package ru.practicum.explore.comment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.comment.dto.CommentDto;
import ru.practicum.explore.comment.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/comments")
@Slf4j
public class CommentPublicController {
    private final CommentService commentService;

    @Autowired
    public CommentPublicController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAllComments(@PathVariable Long eventId,
                                           @RequestParam List<Long> users,
                                           @RequestParam(required = false) String text,
                                           @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                               LocalDateTime rangeStart,
                                           @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                               LocalDateTime rangeEnd,
                                           @RequestParam(required = false, defaultValue = "0") Integer from,
                                           @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: '/comment/events/{eventId}' на получение " +
                "всех комментариев события с возможностью фильтрации.");
        return commentService.getAllComments(eventId, users, text, rangeStart, rangeEnd, from, size);
    }
}
