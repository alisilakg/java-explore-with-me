package ru.practicum.explore.exceptions;

public class StatsRequestException extends RuntimeException {
    public StatsRequestException(String message) {
        super(message);
    }
}
