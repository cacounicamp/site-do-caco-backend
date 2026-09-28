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

import java.util.Collection;
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

    /**
     * Quantas respostas SUBMETIDAS (submittedAt preenchido) existem por pergunta, para todo tipo —
     * texto não vazio, ao menos uma opção marcada, ou arquivo anexado contam igualmente como "respondida".
     * Não considera visibilidade condicional: uma pergunta condicional some do payload quando oculta,
     * então "respondida" aqui só diz quantas vezes ela foi de fato preenchida, não sobre quantas
     * submissões ela era aplicável (isso é cálculo de coorte, fora do escopo desta consulta simples).
     */
    @Query("""
            select a.question.id as questionId, count(a) as total
            from FormAnswer a
            where a.question.form = :form and a.submission.submittedAt is not null
            group by a.question.id
            """)
    List<QuestionAnswerCount> countAnsweredGroupedByQuestionForForm(@Param("form") Form form);

    /** Distribuição de valores para perguntas TEXT com formato INTEGER (ex.: ano de ingresso). */
    @Query("""
            select a.question.id as questionId, a.textValue as value, count(a) as total
            from FormAnswer a
            where a.question.id in :questionIds and a.submission.submittedAt is not null
            group by a.question.id, a.textValue
            """)
    List<TextValueCount> countTextValuesGroupedByQuestion(@Param("questionIds") Collection<UUID> questionIds);

    interface QuestionAnswerCount {
        UUID getQuestionId();
        long getTotal();
    }

    interface TextValueCount {
        UUID getQuestionId();
        String getValue();
        long getTotal();
    }
}
