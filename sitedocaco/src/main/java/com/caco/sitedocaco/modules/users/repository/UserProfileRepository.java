package com.caco.sitedocaco.modules.users.repository;

import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.modules.users.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUser(User user);
    boolean existsByUser(User user);
    boolean existsByUserEmail(String email);
}

