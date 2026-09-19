package com.caco.sitedocaco.features.forms.repository;

import com.caco.sitedocaco.features.forms.entity.OptionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptionSetRepository extends JpaRepository<OptionSet, UUID> {
    Optional<OptionSet> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<OptionSet> findAllByOrderBySlugAsc();
}
