package com.caco.sitedocaco.modules.exams.service;

import com.caco.sitedocaco.modules.exams.dto.request.CreateExamDTO;
import com.caco.sitedocaco.modules.exams.dto.request.UpdateExamDTO;
import com.caco.sitedocaco.modules.exams.dto.response.ExamWithoutSubjectDTO;
import com.caco.sitedocaco.modules.exams.entity.Exam;
import com.caco.sitedocaco.modules.exams.entity.Professor;
import com.caco.sitedocaco.modules.exams.entity.Subject;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.exams.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final SubjectService subjectService;
    private final ProfessorService professorService;

    @Transactional
    public Exam createExam(CreateExamDTO dto) {
        Subject subject = subjectService.getSubjectByCode(dto.subjectCode());

        Exam exam = new Exam();
        exam.setSubject(subject);
        exam.setYear(dto.year());
        exam.setType(dto.type());
        exam.setFileUrl(dto.fileUrl());

        if (dto.professorId() != null) {
            Professor professor = professorService.getProfessorById(dto.professorId());
            exam.setProfessor(professor);
        }

        return examRepository.save(exam);
    }

    @Transactional(readOnly = true)
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Exam> getAllExams(Pageable pageable) {
        return examRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Exam> getAllExams(Integer year, UUID professorId, String subjectCode, Pageable pageable) {
        String normalizedSubjectCode = subjectCode == null ? null : subjectCode.trim().toUpperCase();
        return examRepository.findAllWithFilters(year, professorId, normalizedSubjectCode, pageable);
    }

    @Transactional(readOnly = true)
    public Exam getExamById(UUID id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prova não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<ExamWithoutSubjectDTO> getExamsBySubject(String subjectCode) {
        Subject subject = subjectService.getSubjectByCode(subjectCode);
        return examRepository.findBySubject(subject)
                .stream()
                .map(exam -> new ExamWithoutSubjectDTO(
                        exam.getId(),
                        exam.getYear(),
                        exam.getType(),
                        exam.getFileUrl()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ExamWithoutSubjectDTO> getExamsBySubject(String subjectCode, Pageable pageable) {
        Subject subject = subjectService.getSubjectByCode(subjectCode);
        return examRepository.findBySubject(subject, pageable)
                .map(exam -> new ExamWithoutSubjectDTO(
                        exam.getId(),
                        exam.getYear(),
                        exam.getType(),
                        exam.getFileUrl()
                ));
    }

    @Transactional
    public Exam updateExam(UUID id, UpdateExamDTO dto) {
        Exam exam = getExamById(id);

        if (dto.subjectCode() != null) {
            Subject newSubject = subjectService.getSubjectByCode(dto.subjectCode());
            exam.setSubject(newSubject);
        }

        if (Boolean.TRUE.equals(dto.removeProfessor())) {
            exam.setProfessor(null);
        } else if (dto.professorId() != null) {
            Professor professor = professorService.getProfessorById(dto.professorId());
            exam.setProfessor(professor);
        }

        if (dto.year() != null) {
            exam.setYear(dto.year());
        }

        if (dto.type() != null) {
            exam.setType(dto.type());
        }

        if (dto.fileUrl() != null) {
            exam.setFileUrl(dto.fileUrl());
        }

        return examRepository.save(exam);
    }

    @Transactional
    public void deleteExam(UUID id) {
        Exam exam = getExamById(id);
        examRepository.delete(exam);
    }

    public List<ExamWithoutSubjectDTO> getExamsBySubjectCode(String subjectCode) {
        List<Exam> exams = examRepository.findBySubjectSubjectCode(subjectCode);
        return exams.stream()
                .map(exam -> new ExamWithoutSubjectDTO(
                        exam.getId(),
                        exam.getYear(),
                        exam.getType(),
                        exam.getFileUrl()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Integer> getAllAvailableYears() {
        return examRepository.findAllDistinctYears();
    }
}