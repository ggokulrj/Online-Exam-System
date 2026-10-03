package com.example.onlineexam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "questions")
@Getter @Setter @NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false) private String optionA;
    @Column(nullable = false) private String optionB;
    @Column(nullable = false) private String optionC;
    @Column(nullable = false) private String optionD;

    @Column(nullable = false, length = 1)
    private String correctOption; // "A", "B", "C" or "D"
}
