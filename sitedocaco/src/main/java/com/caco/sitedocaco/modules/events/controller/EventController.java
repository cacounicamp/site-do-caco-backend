package com.caco.sitedocaco.modules.events.controller;

import com.caco.sitedocaco.modules.events.dto.response.EventResponseDTO;
import com.caco.sitedocaco.modules.events.dto.response.EventSummaryDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.events.service.EventService;
import com.caco.sitedocaco.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/public/events")
@RequiredArgsConstructor
@RateLimit
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventSummaryDTO>> getUpcomingEvents(
            @PageableDefault(size = 10, sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<EventSummaryDTO> events = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/past")
    public ResponseEntity<Page<EventSummaryDTO>> getPastEvents(
            @PageableDefault(size = 10, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EventSummaryDTO> events = eventService.getPastEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEventById(
            @PathVariable UUID eventId,
            @RequestAttribute(value = "userId", required = false) UUID userId) {
        EventResponseDTO event = eventService.getEventById(eventId, userId);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<EventResponseDTO> getEventBySlug(
            @PathVariable String slug) {
        UUID userId = null;
        try{
            userId = userService.getCurrentUser().getId();
        } catch (Exception e){} // Se não tiver usuário autenticado, userId permanece null

        EventResponseDTO event = eventService.getEventBySlug(slug, userId);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/month")
    public ResponseEntity<Page<EventSummaryDTO>> getEventsByMonth(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.ASC) Pageable pageable) {

        // Se date for fornecido, usa year e month da date
        if (date != null) {
            year = date.getYear();
            month = date.getMonthValue();
        }

        // Se year e month não forem fornecidos, usa o mês atual
        if (year == null || month == null) {
            LocalDate currentDate = LocalDate.now();
            year = currentDate.getYear();
            month = currentDate.getMonthValue();
        }

        Page<EventSummaryDTO> events = eventService.getEventsByMonthWithMargin(year, month, pageable);
        return ResponseEntity.ok(events);
    }
}