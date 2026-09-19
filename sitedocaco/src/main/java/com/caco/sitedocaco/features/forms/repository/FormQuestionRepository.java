package com.caco.sitedocaco.features.forms.repository;

import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.OptionSet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormQuestionRepository extends JpaRepository<FormQuestion, UUID> {

    @EntityGraph(attributePaths = {"optionSet", "showIfQuestion", "showIfOption"})
    List<FormQuestion> findByFormOrderByDisplayOrderAscIdAsc(Form form);

    @EntityGraph(attributePaths = {"optionSet", "showIfQuestion", "showIfOption"})
    List<FormQuestion> findByFormAndActiveTrueOrderByDisplayOrderAscIdAsc(Form form);

    Optional<FormQuestion> findByIdAndForm(UUID id, Form form);
    Optional<FormQuestion> findByFormAndCode(Form form, String code);
    boolean existsByFormAndCode(Form form, String code);
    long countByFormAndActiveTrue(Form form);

    boolean existsByOptionSet(OptionSet optionSet);
    boolean existsByShowIfQuestion(FormQuestion question);
    boolean existsByShowIfQuestionAndActiveTrue(FormQuestion question);
    boolean existsByShowIfOption(FormOption option);
    boolean existsByShowIfOptionAndActiveTrue(FormOption option);

    @Query("select max(q.displayOrder) from FormQuestion q where q.form = :form")
    Integer findMaxDisplayOrder(@Param("form") Form form);
}
