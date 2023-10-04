package ru.practicum.explore.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.comment.dto.CommentDto;
import ru.practicum.explore.comment.dto.NewCommentDto;
import ru.practicum.explore.comment.dto.UpdateCommentDto;
import ru.practicum.explore.comment.service.CommentService;

import static ru.practicum.explore.validation.ValidationGroups.Create;
import static ru.practicum.explore.validation.ValidationGroups.Update;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommentPrivateController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@Validated(Create.class) @RequestBody NewCommentDto newCommentDto,
                                    @PathVariable Long userId) {
        log.info("Получен POST-запрос к эндпоинту: '/users/{userId}/comments' на добавление комментария " +
                "пользователем с ID={}", userId);
        return commentService.createComment(newCommentDto, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAllCommentsByUserId(@PathVariable Long userId,
                                                   @RequestParam(required = false, defaultValue = "0") Integer from,
                                                   @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: '/users/{userId}/comments' на получение всех комментариев " +
                "пользователя с ID={}", userId);
        return commentService.getAllCommentsByUserId(userId, from, size);
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getCommentById(@PathVariable Long userId,
                                     @PathVariable Long commentId) {
        log.info("Получен GET-запрос к эндпоинту: '/users/{userId}/comments/{commentId}' на получение " +
                "комментария с ID={}", commentId);
        return commentService.getCommentById(userId, commentId);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@Validated(Update.class) @RequestBody UpdateCommentDto updateCommentDto,
                                    @PathVariable Long commentId,
                                    @PathVariable Long userId) {
        log.info("Получен PATCH-запрос к эндпоинту: '/users/{userId}/comments/{commentId}/events/{eventId}' на изменение комментария " +
                "с ID={} пользователем с ID={}", commentId, userId);
        return commentService.updateComment(updateCommentDto, userId, commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId,
                              @PathVariable Long userId) {
        log.info("Получен DELETE-запрос к эндпоинту: '/users/{userId}/comments/{commentId}' на удаление комментария " +
                "с ID={} пользователем с ID={}", commentId, userId);
        commentService.deleteComment(userId, commentId);
    }
}
