package com.example.onlineexam.dto;

import com.example.onlineexam.model.Attempt;

import java.time.LocalDateTime;

public record ResultResponse(Long attemptId, String examTitle, Integer score, Integer totalQuestions,
                             Double percentage, Boolean passed, LocalDateTime submittedAt) {
    public static ResultResponse from(Attempt a) {
        return new ResultResponse(a.getId(), a.getExam().getTitle(), a.getScore(), a.getTotalQuestions(),
                a.getPercentage(), a.getPassed(), a.getSubmittedAt());
    }
}
