package com.example.onlineexam.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ExamRequest(
        @NotBlank String title,
        String description,
        @Min(1) int durationMinutes,
        @Min(1) @Max(100) int passPercentage) {
}
