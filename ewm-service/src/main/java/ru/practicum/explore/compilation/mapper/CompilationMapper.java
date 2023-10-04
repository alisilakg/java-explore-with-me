package ru.practicum.explore.compilation.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.NewCompilationDto;
import ru.practicum.explore.compilation.model.Compilation;
import ru.practicum.explore.event.mapper.EventMapper;
import ru.practicum.explore.event.repository.EventRepository;


@Component
public class CompilationMapper {
    private final EventRepository eventRepository;

    @Autowired
    public CompilationMapper(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }


    public Compilation toCompilation(NewCompilationDto newCompilationDto) {
        return Compilation.builder()
                .pinned(newCompilationDto.getPinned())
                .title(newCompilationDto.getTitle())
                .events(eventRepository.findAllById(newCompilationDto.getEvents()))
                .build();
    }


    public CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(EventMapper.toEventShortDto(compilation.getEvents()))
                .build();
    }


}

