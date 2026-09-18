package com.caco.sitedocaco.shared.contract;

import com.caco.sitedocaco.modules.users.entity.User;

import java.util.UUID;

public interface UserAccess {
    User getCurrentUser();
    User getUserById(UUID userId);
    User getUserByEmail(String email);
}
