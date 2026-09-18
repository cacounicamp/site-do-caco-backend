package com.caco.sitedocaco.modules.users.dto.response;

import com.caco.sitedocaco.modules.users.entity.UserProfile;
import com.caco.sitedocaco.shared.entity.CourseType;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        CourseType course,
        String otherCourseName,
        int entryYear,
        LocalDateTime createdAt
) {
    public static UserProfileResponse fromEntity(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getCourse(),
                profile.getOtherCourseName(),
                profile.getEntryYear(),
                profile.getCreatedAt()
        );
    }
}

