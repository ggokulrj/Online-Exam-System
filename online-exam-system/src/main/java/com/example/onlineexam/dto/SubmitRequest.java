package com.example.onlineexam.dto;

import java.util.Map;

/** answers: questionId -> chosen option ("A".."D"). Example: {"answers": {"1": "B", "2": "D"}} */
public record SubmitRequest(Map<Long, String> answers) {
}
