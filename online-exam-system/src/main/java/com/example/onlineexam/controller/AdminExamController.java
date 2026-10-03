package com.example.onlineexam.controller;

import com.example.onlineexam.dto.*;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.service.ExamService;
import com.example.onlineexam.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/exams")
@RequiredArgsConstructor
public class AdminExamController {

    private final ExamService examService;
    private final ReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Exam createExam(@Valid @RequestBody ExamRequest req) {
        return examService.createExam(req);
    }

    @PostMapping("/`{examId}/questions`")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse addQuestion(@PathVariable Long examId, @Valid @RequestBody QuestionRequest req) {
        return examService.addQuestion(examId, req);
    }

    @GetMapping("/{examId}/report")
    public ExamReport report(@PathVariable Long examId) {
        return reportService.examReport(examId);
    }
}
