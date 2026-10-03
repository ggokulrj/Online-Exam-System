package com.example.onlineexam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QuestionRequest(
        @NotBlank String text,
        @NotBlank String optionA,
        @NotBlank String optionB,
        @NotBlank String optionC,
        @NotBlank String optionD,
        @NotBlank @Pattern(regexp = "[ABCD]", message = "must be A, B, C or D") String correctOption) {
}
