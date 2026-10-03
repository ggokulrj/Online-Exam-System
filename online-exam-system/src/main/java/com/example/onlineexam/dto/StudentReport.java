package com.example.onlineexam.dto;

import java.util.List;

public record StudentReport(int totalAttempts, double averagePercentage, long passedCount, List<ResultResponse> attempts) {
}
