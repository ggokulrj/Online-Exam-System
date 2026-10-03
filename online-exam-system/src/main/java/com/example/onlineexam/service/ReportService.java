package com.example.onlineexam.service;

import com.example.onlineexam.dto.ExamReport;
import com.example.onlineexam.dto.ResultResponse;
import com.example.onlineexam.dto.StudentReport;
import com.example.onlineexam.exception.ApiException;
import com.example.onlineexam.model.Attempt;
import com.example.onlineexam.model.AttemptStatus;
import com.example.onlineexam.model.Exam;
import com.example.onlineexam.repository.AttemptRepository;
import com.example.onlineexam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AttemptRepository attemptRepository;
    private final ExamRepository examRepository;

    /** A student's own performance across all submitted exams. */
    public StudentReport studentReport(String username) {
        List<Attempt> attempts = attemptRepository
                .findByUserUsernameAndStatusOrderBySubmittedAtDesc(username, AttemptStatus.SUBMITTED);
        double avg = attempts.stream().mapToDouble(Attempt::getPercentage).average().orElse(0);
        long passed = attempts.stream().filter(Attempt::getPassed).count();
        return new StudentReport(attempts.size(), round(avg), passed,
                attempts.stream().map(ResultResponse::from).toList());
    }

    /** Admin view: how everyone did in one exam. */
    public ExamReport examReport(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exam not found"));
        List<Attempt> attempts = attemptRepository.findByExamIdAndStatus(examId, AttemptStatus.SUBMITTED);

        DoubleSummaryStatistics stats = attempts.stream().mapToDouble(Attempt::getPercentage).summaryStatistics();
        long passed = attempts.stream().filter(Attempt::getPassed).count();
        int count = attempts.size();
        return new ExamReport(exam.getId(), exam.getTitle(), count,
                round(stats.getAverage()),
                count == 0 ? 0 : stats.getMax(),
                count == 0 ? 0 : stats.getMin(),
                passed, count - passed);
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
