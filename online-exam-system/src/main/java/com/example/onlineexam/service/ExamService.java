package com.example.onlineexam.service;

import com.example.onlineexam.dto.ExamRequest;
import com.example.onlineexam.dto.QuestionRequest;
import com.example.onlineexam.dto.QuestionResponse;
import com.example.onlineexam.exception.ApiException;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.model.Question;
import com.example.onlineexam.repository.ExamRepository;
import com.example.onlineexam.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public Exam createExam(ExamRequest req) {
        Exam exam = new Exam();
        exam.setTitle(req.title());
        exam.setDescription(req.description());
        exam.setDurationMinutes(req.durationMinutes());
        exam.setPassPercentage(req.passPercentage());
        return examRepository.save(exam);
    }

    public QuestionResponse addQuestion(Long examId, QuestionRequest req) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exam not found"));
        Question q = new Question();
        q.setExam(exam);
        q.setText(req.text());
        q.setOptionA(req.optionA());
        q.setOptionB(req.optionB());
        q.setOptionC(req.optionC());
        q.setOptionD(req.optionD());
        q.setCorrectOption(req.correctOption());
        return QuestionResponse.from(questionRepository.save(q));
    }

    public List<Exam> listExams() {
        return examRepository.findAll();
    }
}
