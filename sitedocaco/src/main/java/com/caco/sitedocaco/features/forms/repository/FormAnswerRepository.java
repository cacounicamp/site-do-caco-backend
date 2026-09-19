package com.caco.sitedocaco.features.forms.repository;

import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormAnswer;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.FormSubmission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormAnswerRepository extends JpaRepository<FormAnswer, UUID> {

    @EntityGraph(attributePaths = {"question", "options", "options.option"})
    List<FormAnswer> findBySubmission(FormSubmission submission);

    Optional<FormAnswer> findBySubmissionAndQuestion(FormSubmission submission, FormQuestion question);

    boolean existsByQuestion(FormQuestion question);

    @Query("select distinct a.question.id from FormAnswer a where a.question.form = :form")
    List<UUID> findQuestionIdsWithAnswers(@Param("form") Form form);
}
