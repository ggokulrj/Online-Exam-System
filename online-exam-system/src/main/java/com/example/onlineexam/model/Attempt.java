package com.example.onlineexam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attempts",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exam_id"}))
@Getter @Setter @NoArgsConstructor
public class Attempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;

    private Integer totalQuestions;
    private Integer score;
    private Double percentage;
    private Boolean passed;

    @Enumerated(EnumType.STRING)
    private AttemptStatus status;

    // Optimistic locking: two simultaneous submits cannot both succeed.
    @Version
    private Long version;
}
