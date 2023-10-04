package ru.practicum.explore.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.NewCompilationDto;
import ru.practicum.explore.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explore.compilation.mapper.CompilationMapper;
import ru.practicum.explore.compilation.model.Compilation;
import ru.practicum.explore.compilation.repository.CompilationRepository;
import ru.practicum.explore.error.exception.ConflictException;
import ru.practicum.explore.error.exception.NotFoundException;
import ru.practicum.explore.event.model.Event;
import ru.practicum.explore.event.repository.EventRepository;
import ru.practicum.explore.event.service.EventService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventService eventService;


    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        Page<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(PageRequest.of(from / size, size));
        } else {
            compilations = compilationRepository.findByPinned(pinned, PageRequest.of(from / size, size));
        }
        for (Compilation compilation : compilations) {
            if (compilation.getEvents() == null) {
                compilation.setEvents(new ArrayList<>());
            } else {
                List<Long> eventsIds = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
                List<Event> events = eventRepository.findAllById(eventsIds);
                List<Event> eventsWithViewsAndRequests = eventService.getEventsWithViewsAndCountRequests(events);
                compilation.setEvents(eventsWithViewsAndRequests);
            }
        }
        return compilations.map(compilationMapper::toCompilationDto).getContent();
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getPinned() == null) {
            newCompilationDto.setPinned(false);
        }
        Compilation compilation = compilationRepository.save(compilationMapper.toCompilation(newCompilationDto));
        List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
        List<Event> eventsWithViewsAndRequests = eventService.getEventsWithViewsAndCountRequests(events);
        compilation.setEvents(eventsWithViewsAndRequests);
        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilationById(Long compId) {
        compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id = " + compId + " не найдена"));
        compilationRepository.deleteById(compId);

    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = findCompilationIfExists(compId);
        List<Long> eventsIds = updateCompilationRequest.getEvents();
        if (!eventsIds.isEmpty() || Objects.nonNull(eventsIds)) {
            List<Event> updatedEvents = getEvents(eventsIds);
            compilation.setEvents(updatedEvents);
        }
        if (Objects.nonNull(updateCompilationRequest.getPinned())) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        String title = updateCompilationRequest.getTitle();
        if (title != null && title.isBlank()) {
            if (compilationRepository.existsByTitleAndIdNot(title, compilation.getId())) {
                throw new ConflictException("Название подборки уже существует и не может быть использовано повторно.");
            }
            compilation.setTitle(title);
        }
        Compilation updatedComp = compilationRepository.save(compilation);
        List<Event> events = eventRepository.findAllById(updateCompilationRequest.getEvents());
        List<Event> eventsWithViewsAndRequests = eventService.getEventsWithViewsAndCountRequests(events);
        updatedComp.setEvents(eventsWithViewsAndRequests);
        return compilationMapper.toCompilationDto(updatedComp);
    }

    private List<Event> getEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(eventRepository.findAllById(eventIds));
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = findCompilationIfExists(compId);
        if (compilation.getEvents() == null) {
            compilation.setEvents(new ArrayList<>());
        } else {
            List<Long> eventsIds = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
            List<Event> events = eventRepository.findAllById(eventsIds);
            List<Event> eventsWithViewsAndRequests = eventService.getEventsWithViewsAndCountRequests(events);
            compilation.setEvents(eventsWithViewsAndRequests);
        }
        return compilationMapper.toCompilationDto(compilation);
    }

    private Compilation findCompilationIfExists(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка событий с id = " + compId + " не найдена"));
    }
}
