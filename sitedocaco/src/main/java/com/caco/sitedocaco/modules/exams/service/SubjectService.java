package com.caco.sitedocaco.modules.exams.service;

import com.caco.sitedocaco.modules.exams.dto.request.CreateSubjectDTO;
import com.caco.sitedocaco.modules.exams.entity.Subject;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.exams.repository.ExamRepository;
import com.caco.sitedocaco.modules.exams.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;

    @Transactional
    public Subject createSubject(CreateSubjectDTO dto) {
        if (subjectRepository.existsBySubjectCode(dto.subjectCode())) {
            throw new BusinessRuleException("Já existe uma disciplina com este código");
        }

        Subject subject = new Subject();
        subject.setSubjectCode(dto.subjectCode().toUpperCase().trim());
        subject.setName(dto.name().trim());

        return subjectRepository.save(subject);
    }

    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Subject> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Subject getSubjectByCode(String subjectCode) {
        return subjectRepository.findBySubjectCode(subjectCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada"));
    }

    @Transactional
    public void deleteSubject(String subjectCode) {
        Subject subject = getSubjectByCode(subjectCode);
        examRepository.deleteBySubjectSubjectCode(subject.getSubjectCode());
        subjectRepository.delete(subject);
    }
}