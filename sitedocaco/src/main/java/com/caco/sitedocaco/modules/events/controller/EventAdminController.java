package com.caco.sitedocaco.modules.events.controller;

import com.caco.sitedocaco.modules.events.dto.request.CreateEventDTO;
import com.caco.sitedocaco.modules.events.dto.request.CreateGalleryItemDTO;
import com.caco.sitedocaco.modules.events.dto.request.UpdateEventDTO;
import com.caco.sitedocaco.modules.events.dto.request.UpdateGalleryItemDTO;
import com.caco.sitedocaco.modules.events.dto.response.EventGalleryItemDTO;
import com.caco.sitedocaco.modules.events.dto.response.EventResponseDTO;
import com.caco.sitedocaco.modules.events.entity.Event;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.events.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RateLimit(capacity = 30, refillTokens = 30)
public class EventAdminController {

    private final EventService eventService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @ModelAttribute CreateEventDTO dto) throws IOException {
        Event event = eventService.createEvent(dto);
        EventResponseDTO response = eventService.getEventById(event.getId(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable UUID eventId,
            @ModelAttribute @Valid UpdateEventDTO dto) throws IOException {
        Event event = eventService.updateEvent(eventId, dto);
        EventResponseDTO response = eventService.getEventById(event.getId(), null);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
    }

    // ========== GALLERY MANAGEMENT ==========

    @PostMapping(value = "/{eventId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventGalleryItemDTO> addGalleryItem(
            @PathVariable UUID eventId,
            @Valid @ModelAttribute CreateGalleryItemDTO dto) throws IOException {
        EventGalleryItemDTO item = eventService.createGalleryItem(eventId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{eventId}/gallery/{itemId}")
    public ResponseEntity<EventGalleryItemDTO> updateGalleryItem(
            @PathVariable UUID eventId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateGalleryItemDTO dto) {
        EventGalleryItemDTO item = eventService.updateGalleryItem(eventId, itemId, dto);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{eventId}/gallery/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGalleryItem(
            @PathVariable UUID eventId,
            @PathVariable UUID itemId) {
        eventService.deleteGalleryItem(eventId, itemId);
    }
}