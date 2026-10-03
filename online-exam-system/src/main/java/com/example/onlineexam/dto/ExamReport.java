package com.example.onlineexam.dto;

public record ExamReport(Long examId, String title, int totalAttempts, double averagePercentage,
                         double highestPercentage, double lowestPercentage, long passCount, long failCount) {
}
