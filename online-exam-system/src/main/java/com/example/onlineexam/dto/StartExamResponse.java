package com.example.onlineexam.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StartExamResponse(Long attemptId, String examTitle, LocalDateTime endsAt, List<QuestionResponse> questions) {
}
