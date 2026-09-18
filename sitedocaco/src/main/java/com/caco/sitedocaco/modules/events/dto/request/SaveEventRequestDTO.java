package com.caco.sitedocaco.modules.events.dto.request;

import com.caco.sitedocaco.modules.events.entity.UserEvent;

public record SaveEventRequestDTO(
        UserEvent.ParticipationStatus status
) {}