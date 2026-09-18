package com.caco.sitedocaco.modules.exams.controller;

import com.caco.sitedocaco.modules.exams.dto.request.CreateExamDTO;
import com.caco.sitedocaco.modules.exams.dto.request.ImportPayloadDTO;
import com.caco.sitedocaco.modules.exams.entity.ExamType;
import com.caco.sitedocaco.modules.exams.entity.Professor;
import com.caco.sitedocaco.modules.exams.repository.ProfessorRepository;
import com.caco.sitedocaco.modules.exams.service.ExamService;
import com.caco.sitedocaco.modules.exams.service.SubjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/temp-import")
@RequiredArgsConstructor
@Slf4j
public class TempImportController {

    private final SubjectService subjectService;
    private final ProfessorRepository professorRepository; // Usamos repo para ter a entidade salva
    private final ExamService examService;

    @PostMapping
    public ResponseEntity<String> importData(@RequestBody ImportPayloadDTO data) {

        // Mapa para traduzir ID do JSON para a Entidade real do Banco
        Map<String, Professor> professorMap = new HashMap<>();

        // 1. Importar Disciplinas (Normal)
        data.subjects().forEach(s -> {
            try { subjectService.createSubject(s); } catch (Exception ignored) {}
        });

        // 2. Importar Professores e Mapear IDs
        for (var pDto : data.professors()) {
            try {
                Professor p = new Professor();
                p.setName(pDto.name().trim());
                // O save gera um NOVO ID do banco. O ID do JSON morre aqui.
                Professor saved = professorRepository.save(p);
                professorMap.put(pDto.id(), saved);
            } catch (Exception e) {
                log.warn("Erro ao importar professor: {}", pDto.name());
            }
        }

        // 3. Importar Provas linkando as referências
        for (var eDto : data.exams()) {
            try {
                // Busca o professor no nosso mapa de "tradução"
                // Se o professorId não existir no mapa, retorna null automaticamente
                Professor professorNoBanco = professorMap.get(eDto.professorId());

                // Criamos o CreateExamDTO usando o ID real que o banco gerou
                CreateExamDTO createDto = new CreateExamDTO(
                        eDto.subjectCode(),
                        professorNoBanco != null ? professorNoBanco.getId() : null,
                        eDto.year(),
                        ExamType.valueOf(eDto.type()),
                        eDto.fileUrl()
                );

                examService.createExam(createDto);
            } catch (Exception ex) {
                log.error("Erro ao linkar prova: {} - {}", eDto.subjectCode(), ex.getMessage());
            }
        }

        return ResponseEntity.ok("Importação finalizada com sucesso!");
    }
}