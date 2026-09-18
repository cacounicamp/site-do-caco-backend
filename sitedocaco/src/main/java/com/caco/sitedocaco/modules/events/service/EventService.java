package com.caco.sitedocaco.modules.events.service;

import com.caco.sitedocaco.modules.events.dto.request.CreateEventDTO;
import com.caco.sitedocaco.modules.events.dto.request.CreateGalleryItemDTO;
import com.caco.sitedocaco.modules.events.dto.request.UpdateEventDTO;
import com.caco.sitedocaco.modules.events.dto.request.UpdateGalleryItemDTO;
import com.caco.sitedocaco.modules.events.dto.response.EventGalleryItemDTO;
import com.caco.sitedocaco.modules.events.dto.response.EventResponseDTO;
import com.caco.sitedocaco.modules.events.dto.response.EventSummaryDTO;
import com.caco.sitedocaco.shared.contract.EventReference;
import com.caco.sitedocaco.shared.contract.UserAccess;
import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.modules.events.entity.Event;
import com.caco.sitedocaco.modules.events.entity.EventGalleryItem;
import com.caco.sitedocaco.modules.events.entity.UserEvent;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.media.infrastructure.ImgBBService;
import com.caco.sitedocaco.modules.events.repository.EventGalleryItemRepository;
import com.caco.sitedocaco.modules.events.repository.EventRepository;
import com.caco.sitedocaco.modules.events.repository.UserEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService implements EventReference {

    private final EventRepository eventRepository;
    private final EventGalleryItemRepository galleryItemRepository;
    private final UserEventRepository userEventRepository;
    private final UserAccess userAccess;
    private final ImgBBService imgBBService;

    @Override
    @Transactional(readOnly = true)
    public Event getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado."));
    }

    // ========== MÉTODOS PÚBLICOS ==========

    @Transactional(readOnly = true)
    public Page<EventSummaryDTO> getUpcomingEvents(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Event> events = eventRepository.findUpcomingEvents(now, pageable);
        return events.map(EventSummaryDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryDTO> getPastEvents(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Event> events = eventRepository.findPastEvents(now, pageable);
        return events.map(EventSummaryDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(UUID eventId, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        // Buscar status do usuário, se autenticado
        UserEvent.ParticipationStatus userStatus = null;
        if (userId != null) {
            Optional<UserEvent> userEvent = userEventRepository.findByUserAndEvent(
                    userAccess.getUserById(userId),
                    event
            );
            userStatus = userEvent.map(UserEvent::getStatus).orElse(null);
        }

        // Buscar galeria
        List<EventGalleryItem> galleryItems = galleryItemRepository.findByEventIdOrderByIdAsc(eventId);
        List<EventGalleryItemDTO> gallery = galleryItems.stream()
                .map(EventGalleryItemDTO::fromEntity)
                .toList();

        return EventResponseDTO.fromEntity(event, gallery, userStatus);
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryDTO> getEventsByMonthWithMargin(int year, int month, Pageable pageable) {
        // Cria YearMonth para o mês especificado
        YearMonth yearMonth = YearMonth.of(year, month);

        // Primeiro dia do mês
        LocalDate firstDayOfMonth = yearMonth.atDay(1);

        // Último dia do mês
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

        // Calcula as datas com margem (7 dias antes e depois)
        LocalDateTime startDateWithMargin = firstDayOfMonth.minusDays(7).atStartOfDay();
        LocalDateTime endDateWithMargin = lastDayOfMonth.plusDays(7).atTime(23, 59, 59);

        // Busca eventos que ocorrem dentro deste intervalo
        Page<Event> events = eventRepository.findEventsByDateRange(
                startDateWithMargin,
                endDateWithMargin,
                pageable
        );

        return events.map(EventSummaryDTO::fromEntity);
    }

    // ========== MÉTODOS PRIVADOS ==========

    @Transactional(readOnly = true)
    public Page<EventSummaryDTO> getUserSavedEvents(UUID userId, Pageable pageable) {
        User user = userAccess.getUserById(userId);

        // Usar o método com paginação e ordenação correta
        Page<UserEvent> userEvents = userEventRepository.findByUserOrderBySavedAtDesc(user, pageable);

        // Converter para EventSummaryDTO
        return userEvents.map(userEvent -> EventSummaryDTO.fromEntity(userEvent.getEvent()));
    }

    @Transactional(readOnly = true)
    public UserEvent getUserEventDetails(UUID eventId, UUID userId) {
        User user = userAccess.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        return userEventRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ResourceNotFoundException("Participação não encontrada"));
    }

    @Transactional
    public void saveEventForUser(UUID eventId, UUID userId, UserEvent.ParticipationStatus status) {
        User user = userAccess.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        Optional<UserEvent> existing = userEventRepository.findByUserAndEvent(user, event);

        UserEvent userEvent;
        if (existing.isPresent()) {
            userEvent = existing.get();
        } else {
            userEvent = new UserEvent();
            userEvent.setUser(user);
            userEvent.setEvent(event);
        }

        userEvent.setStatus(status);
        userEventRepository.save(userEvent);
    }

    @Transactional
    public void unsaveEventForUser(UUID eventId, UUID userId) {
        User user = userAccess.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        userEventRepository.deleteByUserAndEvent(user, event);
    }

    @Transactional
    public void updateParticipationStatus(UUID eventId, UUID userId, UserEvent.ParticipationStatus status) {
        User user = userAccess.getUserById(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        UserEvent userEvent = userEventRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ResourceNotFoundException("Participação não encontrada"));

        userEvent.setStatus(status);
        userEventRepository.save(userEvent);
    }

    // ========== MÉTODOS ADMIN ==========

    @Transactional
    public Event createEvent(CreateEventDTO dto) throws IOException {
        validateEventDates(dto.startDate(), dto.endDate());

        Event event = new Event();
        event.setTitle(dto.title());
        event.setSlug(dto.slug());
        event.setDescription(dto.description());
        event.setStartDate(dto.startDate());
        event.setEndDate(dto.endDate());
        event.setLocation(dto.location());

        String coverImageUrl = null;
        if( dto.coverImage() != null)
            coverImageUrl = imgBBService.uploadImage(dto.coverImage());

        event.setCoverImage(coverImageUrl);
        event.setType(dto.type());
        event.setImportance(dto.importance());
        event.setStatus(Event.EventStatus.SCHEDULED);

        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(UUID eventId, UpdateEventDTO dto) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        if (dto.startDate() != null && dto.endDate() != null) {
            validateEventDates(dto.startDate(), dto.endDate());
        }

        if (dto.title() != null) event.setTitle(dto.title());
        if (dto.slug() != null) event.setSlug(dto.slug());
        if (dto.description() != null) event.setDescription(dto.description());
        if (dto.startDate() != null) event.setStartDate(dto.startDate());
        if (dto.endDate() != null) event.setEndDate(dto.endDate());
        if (dto.location() != null) event.setLocation(dto.location());
        if (dto.locationUrl() != null) event.setLocationUrl(dto.locationUrl());
        if (dto.removeCoverImage() != null && dto.removeCoverImage()) event.setCoverImage(null);
        if (dto.coverImage() != null) {
            String coverImageUrl = imgBBService.uploadImage(dto.coverImage());
            event.setCoverImage(coverImageUrl);
        }
        if (dto.type() != null) event.setType(dto.type());
        if (dto.importance() != null) event.setImportance(dto.importance());
        if (dto.status() != null) event.setStatus(dto.status());


        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Evento não encontrado");
        }

        eventRepository.deleteById(eventId);
    }

    // ========== GALLERY MANAGEMENT ==========

    @Transactional
    public EventGalleryItemDTO createGalleryItem(UUID eventId, CreateGalleryItemDTO dto) throws IOException {
        // Validar DTO
        dto.validate();

        // Verificar se o evento existe
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        EventGalleryItem galleryItem = new EventGalleryItem();
        galleryItem.setEvent(event);
        galleryItem.setType(dto.type());
        galleryItem.setCaption(dto.caption());

        // Se recebeu multipart, fazer upload da imagem
        if (dto.image() != null) {
            String uploadedUrl = imgBBService.uploadImage(dto.image());
            galleryItem.setMediaUrl(uploadedUrl);
        } else {
            // Se recebeu URL direta
            galleryItem.setMediaUrl(dto.mediaUrl());
        }

        EventGalleryItem saved = galleryItemRepository.save(galleryItem);
        return EventGalleryItemDTO.fromEntity(saved);
    }

    @Transactional
    public EventGalleryItemDTO updateGalleryItem(UUID eventId, UUID itemId, UpdateGalleryItemDTO dto) {
        // Verificar se o evento existe
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Evento não encontrado");
        }

        // Buscar item da galeria
        EventGalleryItem galleryItem = galleryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item da galeria não encontrado"));

        // Verificar se o item pertence ao evento
        if (!galleryItem.getEvent().getId().equals(eventId)) {
            throw new BusinessRuleException("Item da galeria não pertence ao evento especificado");
        }

        // Atualizar apenas a legenda
        if (dto.caption() != null) {
            galleryItem.setCaption(dto.caption());
        }

        EventGalleryItem saved = galleryItemRepository.save(galleryItem);
        return EventGalleryItemDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteGalleryItem(UUID eventId, UUID itemId) {
        // Verificar se o evento existe
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Evento não encontrado");
        }

        // Buscar item da galeria
        EventGalleryItem galleryItem = galleryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item da galeria não encontrado"));

        // Verificar se o item pertence ao evento
        if (!galleryItem.getEvent().getId().equals(eventId)) {
            throw new BusinessRuleException("Item da galeria não pertence ao evento especificado");
        }

        galleryItemRepository.delete(galleryItem);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void validateEventDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException("A data de término deve ser após a data de início");
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Executa todo início de hora
    @Transactional
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<Event> allEvents = eventRepository.findAll();
        for (Event event : allEvents) {
            Event.EventStatus newStatus = calculateEventStatus(event, now);
            if (event.getStatus() != newStatus) {
                event.setStatus(newStatus);
                eventRepository.save(event);
            }
        }
    }

    private Event.EventStatus calculateEventStatus(Event event, LocalDateTime now) {
        if (event.getStartDate().isAfter(now)) {
            return Event.EventStatus.SCHEDULED;
        } else if (event.getEndDate().isAfter(now)) {
            return Event.EventStatus.HAPPENING;
        } else {
            return Event.EventStatus.ENDED;
        }
    }

    public EventResponseDTO getEventBySlug(String slug, UUID userId) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        // Buscar status do usuário, se autenticado
        UserEvent.ParticipationStatus userStatus = null;
        if (userId != null) {
            Optional<UserEvent> userEvent = userEventRepository.findByUserAndEvent(
                    userAccess.getUserById(userId),
                    event
            );
            userStatus = userEvent.map(UserEvent::getStatus).orElse(null);
        }

        // Buscar galeria
        List<EventGalleryItem> galleryItems = galleryItemRepository.findByEventIdOrderByIdAsc(event.getId());
        List<EventGalleryItemDTO> gallery = galleryItems.stream()
                .map(EventGalleryItemDTO::fromEntity)
                .toList();

        return EventResponseDTO.fromEntity(event, gallery, userStatus);
    }
}
