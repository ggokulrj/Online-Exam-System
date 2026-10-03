package com.example.onlineexam.service;

import com.example.onlineexam.dto.QuestionResponse;
import com.example.onlineexam.dto.ResultResponse;
import com.example.onlineexam.dto.StartExamResponse;
import com.example.onlineexam.dto.SubmitRequest;
import com.example.onlineexam.exception.ApiException;
import com.example.onlineexam.model.*;
import com.example.onlineexam.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private static final long GRACE_SECONDS = 30; // allowance for network delay

    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;

    @Transactional
    public StartExamResponse start(String username, Long examId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exam not found"));

        if (attemptRepository.existsByUserIdAndExamId(user.getId(), examId)) {
            throw new ApiException(HttpStatus.CONFLICT, "You have already attempted this exam");
        }
        List<Question> questions = new ArrayList<>(questionRepository.findByExamId(examId));
        if (questions.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This exam has no questions yet");
        }
        Collections.shuffle(questions); // each student sees a different order

        Attempt attempt = new Attempt();
        attempt.setUser(user);
        attempt.setExam(exam);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setTotalQuestions(questions.size());
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attemptRepository.save(attempt);

        LocalDateTime endsAt = attempt.getStartedAt().plusMinutes(exam.getDurationMinutes());
        return new StartExamResponse(attempt.getId(), exam.getTitle(), endsAt,
                questions.stream().map(QuestionResponse::from).toList());
    }

    @Transactional
    public ResultResponse submit(String username, Long attemptId, SubmitRequest req) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attempt not found"));

        if (!attempt.getUser().getUsername().equals(username)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This attempt does not belong to you");
        }
        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new ApiException(HttpStatus.CONFLICT, "This attempt was already submitted");
        }

        Exam exam = attempt.getExam();
        LocalDateTime deadline = attempt.getStartedAt()
                .plusMinutes(exam.getDurationMinutes()).plusSeconds(GRACE_SECONDS);
        boolean late = LocalDateTime.now().isAfter(deadline);

        // Calculate the score on the server. Answers submitted after the deadline are ignored.
        int score = 0;
        if (!late) {
            Map<Long, String> answers = (req == null || req.answers() == null) ? Map.of() : req.answers();
            for (Question q : questionRepository.findByExamId(exam.getId())) {
                String given = answers.get(q.getId());
                if (given != null && given.trim().equalsIgnoreCase(q.getCorrectOption())) {
                    score++;
                }
            }
        }

        int total = attempt.getTotalQuestions();
        double percentage = total == 0 ? 0 : Math.round(score * 10000.0 / total) / 100.0;

        attempt.setScore(score);
        attempt.setPercentage(percentage);
        attempt.setPassed(percentage >= exam.getPassPercentage());
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attemptRepository.save(attempt);

        return ResultResponse.from(attempt);
    }
}
