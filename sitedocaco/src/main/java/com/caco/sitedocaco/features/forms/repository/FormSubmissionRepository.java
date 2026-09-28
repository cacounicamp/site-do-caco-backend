package com.caco.sitedocaco.features.forms.repository;

import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormSubmission;
import com.caco.sitedocaco.features.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {

    /** Modo de resposta única: no máximo uma linha deveria existir; pega a mais antiga se, por algum motivo, houver mais. */
    Optional<FormSubmission> findFirstByFormAndUserOrderByCreatedAtAsc(Form form, User user);

    /** Modo de múltiplas respostas: todas as entradas do usuário, da mais antiga para a mais nova. */
    List<FormSubmission> findByFormAndUserOrderByCreatedAtAsc(Form form, User user);

    /** Busca uma entrada específica já validando que pertence ao (formulário, usuário) — evita vazar existência de entrada alheia. */
    Optional<FormSubmission> findByIdAndFormAndUser(UUID id, Form form, User user);

    long countByFormAndUser(Form form, User user);

    boolean existsByForm(Form form);
    long countByForm(Form form);

    /** Só conta entradas efetivamente enviadas — um rascunho (arquivo anexado, nunca enviado) não é uma resposta real. */
    long countByFormAndSubmittedAtIsNotNull(Form form);

    @Query("select count(distinct s.user) from FormSubmission s where s.form = :form and s.submittedAt is not null")
    long countDistinctRespondentsByForm(@Param("form") Form form);
}
