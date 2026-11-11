package com.caco.sitedocaco.features.forms.repository;

import com.caco.sitedocaco.features.forms.entity.FormOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormOptionRepository extends JpaRepository<FormOption, UUID> {
    List<FormOption> findByOptionSetIdInOrderByDisplayOrderAscIdAsc(Collection<UUID> optionSetIds);
    Optional<FormOption> findByOptionSetIdAndCode(UUID optionSetId, String code);
}
