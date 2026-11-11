package com.caco.sitedocaco.features.forms.repository;

import com.caco.sitedocaco.features.forms.entity.FormAnswerOption;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FormAnswerOptionRepository extends JpaRepository<FormAnswerOption, UUID> {
    boolean existsByOption(FormOption option);
}
