package com.caco.sitedocaco.modules.events.dto.response;

public record EventParticipationStatsDTO(
        long interestedCount,
        long goingCount,
        long notGoingCount,
        long totalParticipants // interested + going
) {}