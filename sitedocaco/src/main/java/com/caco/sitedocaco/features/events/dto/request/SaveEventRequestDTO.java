package com.caco.sitedocaco.features.events.dto.request;

import com.caco.sitedocaco.features.events.entity.UserEvent;

public record SaveEventRequestDTO(
        UserEvent.ParticipationStatus status
) {}