package com.caco.sitedocaco.features.forms.repository;

import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormAnswerOption;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FormAnswerOptionRepository extends JpaRepository<FormAnswerOption, UUID> {
    boolean existsByOption(FormOption option);

    /** Quantas vezes cada opção foi marcada, em respostas SUBMETIDAS, para todas as perguntas de escolha do formulário. */
    @Query("""
            select ao.answer.question.id as questionId, ao.option.id as optionId, count(ao) as total
            from FormAnswerOption ao
            where ao.answer.question.form = :form and ao.answer.submission.submittedAt is not null
            group by ao.answer.question.id, ao.option.id
            """)
    List<OptionCount> countGroupedByOptionForForm(@Param("form") Form form);

    interface OptionCount {
        UUID getQuestionId();
        UUID getOptionId();
        long getTotal();
    }
}
