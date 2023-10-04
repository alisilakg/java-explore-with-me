package ru.practicum.explore.pagination;

import org.springframework.data.domain.PageRequest;

public class CustomPageRequest extends PageRequest {
    private final int from;

    public CustomPageRequest(int from, int size) {
        super(from / size, size, null);
        this.from = from;
    }

    @Override
    public long getOffset() {
        return from;
    }
}