package ru.practicum.explore.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.request.dto.ParticipationRequestDto;
import ru.practicum.explore.request.model.Request;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class RequestMapper {
    public ParticipationRequestDto toRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> toRequestDto(List<Request> requests) {
        if (requests.isEmpty()) {
            return emptyList();
        }
        return requests.stream().map(RequestMapper::toRequestDto).collect(toList());
    }
}
