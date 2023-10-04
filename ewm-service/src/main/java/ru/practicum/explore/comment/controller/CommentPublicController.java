package ru.practicum.explore.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.comment.dto.CommentDto;
import ru.practicum.explore.comment.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentPublicController {
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final CommentService commentService;

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getAllComments(@PathVariable Long eventId,
                                           @RequestParam List<Long> users,
                                           @RequestParam(required = false) String text,
                                           @RequestParam(required = false)
                                               @DateTimeFormat(pattern = TIME_FORMAT)
                                               LocalDateTime rangeStart,
                                           @RequestParam(required = false)
                                               @DateTimeFormat(pattern = TIME_FORMAT)
                                               LocalDateTime rangeEnd,
                                           @PositiveOrZero
                                           @RequestParam(defaultValue = "0") Integer from,
                                           @Positive
                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: '/comment/events/{eventId}' на получение " +
                "всех комментариев события с возможностью фильтрации.");
        return commentService.getAllComments(eventId, users, text, rangeStart, rangeEnd, from, size);
    }
}
