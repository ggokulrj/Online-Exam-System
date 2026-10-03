package com.example.onlineexam.repository;

import com.example.onlineexam.model.Attempt;
import com.example.onlineexam.model.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    boolean existsByUserIdAndExamId(Long userId, Long examId);
    List<Attempt> findByUserUsernameAndStatusOrderBySubmittedAtDesc(String username, AttemptStatus status);
    List<Attempt> findByExamIdAndStatus(Long examId, AttemptStatus status);
}
