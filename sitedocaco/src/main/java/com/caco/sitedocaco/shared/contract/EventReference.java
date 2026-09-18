package com.caco.sitedocaco.shared.contract;

import com.caco.sitedocaco.modules.events.entity.Event;

import java.util.UUID;

public interface EventReference {
    Event getEvent(UUID eventId);
}
