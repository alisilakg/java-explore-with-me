package ru.practicum.explore.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.explore.error.exception.ConflictException;
import ru.practicum.explore.error.exception.NotFoundException;
import ru.practicum.explore.error.exception.ValidationException;
import ru.practicum.explore.error.model.ApiError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .status(HttpStatus.NOT_FOUND)
                .reason("The required object was not found.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .status(HttpStatus.BAD_REQUEST)
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(final ConflictException e) {
        log.error(e.getMessage(), e);
        List<String> exception = new ArrayList<>();
        for (StackTraceElement element : e.getStackTrace())
            exception.add(element.toString());
        return ApiError.builder()
                .errors(exception)
                .status(HttpStatus.CONFLICT)
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
