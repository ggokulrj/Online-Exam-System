package com.example.onlineexam.controller;

import com.example.onlineexam.dto.StartExamResponse;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.service.AttemptService;
import com.example.onlineexam.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final AttemptService attemptService;

    @GetMapping
    public List<Exam> listExams() {
        return examService.listExams();
    }

    @PostMapping("/{examId}/start")
    public StartExamResponse start(@PathVariable Long examId, Authentication auth) {
        return attemptService.start(auth.getName(), examId);
    }
}
