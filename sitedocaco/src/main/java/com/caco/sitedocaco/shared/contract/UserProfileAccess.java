package com.caco.sitedocaco.shared.contract;

import com.caco.sitedocaco.modules.users.entity.UserProfile;

import java.util.Optional;

public interface UserProfileAccess {
    Optional<UserProfile> findMyProfile();
}
