package ru.practicum.explore.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.explore.pagination.CustomPageRequest;

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
    private final EventRepository eventRepository;
    private final EventService eventService;


    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = CustomPageRequest.of(from, size);
        Page<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageRequest);
        } else {
            compilations = compilationRepository.findByPinned(pinned, pageRequest);
        }
        setCompilationsWithEventWithViewsAndRequests(compilations.getContent());
        return compilations.map(CompilationMapper::toCompilationDto).getContent();
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getPinned() == null) {
            newCompilationDto.setPinned(false);
        }
        List<Long> eventsIds = newCompilationDto.getEvents();
        List<Event> events = eventRepository.findAllById(eventsIds);
        Compilation compilation = compilationRepository.save(CompilationMapper.toCompilation(newCompilationDto, events));
        setEvents(compilation);
        return CompilationMapper.toCompilationDto(compilation);
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
        setEvents(updatedComp);
        return CompilationMapper.toCompilationDto(updatedComp);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = findCompilationIfExists(compId);
        setEvents(compilation);
        return CompilationMapper.toCompilationDto(compilation);
    }

    private Compilation findCompilationIfExists(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Подборка событий с id = " + compId + " не найдена"));
    }

    private void setEvents(Compilation compilation) {
        if (compilation.getEvents() == null) {
            compilation.setEvents(new ArrayList<>());
        } else {
            List<Long> eventsIds = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
            List<Event> events = eventRepository.findAllById(eventsIds);
            List<Event> eventsWithViewsAndRequests = eventService.getEventsWithViewsAndCountRequests(events);
            compilation.setEvents(eventsWithViewsAndRequests);
        }
    }

    private List<Event> getEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(eventRepository.findAllById(eventIds));
    }

    private void setCompilationsWithEventWithViewsAndRequests(List<Compilation> compilations) {
        List<Event> events = new ArrayList<>();
        for (Compilation compilation : compilations) {
            List<Event> eventList = compilation.getEvents();
            events.addAll(eventList);
        }
        List<Event> eventsWithViewsAndRequests = eventService.getEventsWithViewsAndCountRequests(events);
        for (Compilation compilation : compilations) {
            Long compilationId = compilation.getId();
            List<Event> eventsForCompilation = eventsWithViewsAndRequests.stream()
                    .filter(event1 -> compilationId.equals(event1.getId())).collect(Collectors.toList());
            compilation.setEvents(eventsForCompilation);
        }
    }

}
