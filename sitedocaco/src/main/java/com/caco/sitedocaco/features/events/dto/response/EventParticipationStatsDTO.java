package com.caco.sitedocaco.features.events.dto.response;

public record EventParticipationStatsDTO(
        long interestedCount,
        long goingCount,
        long notGoingCount,
        long totalParticipants // interested + going
) {}