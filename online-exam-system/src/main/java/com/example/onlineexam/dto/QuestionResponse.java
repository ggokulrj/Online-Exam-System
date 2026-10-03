package com.example.onlineexam.dto;

import com.example.onlineexam.model.Question;

public record QuestionResponse(Long id, String text, String optionA, String optionB, String optionC, String optionD) {
    public static QuestionResponse from(Question q) {
        return new QuestionResponse(q.getId(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD());
    }
}
