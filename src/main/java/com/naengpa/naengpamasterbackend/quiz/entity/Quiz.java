package com.naengpa.naengpamasterbackend.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "statement", nullable = false)
    private String statement;

    @Column(name = "answer", nullable = false)
    private Boolean answer;

    @Column(name = "explanation")
    private String explanation;

    @Column(name = "source_product_name", length = 50)
    private String sourceProductName;

    @Column(name = "source_product_id")
    private Long sourceProductId;

    @Column(name = "quiz_date", nullable = false, unique = true)
    private LocalDate quizDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    public static Quiz create(String statement, Boolean answer, String explanation,
                              String sourceProductName, Long sourceProductId, LocalDate quizDate) {
        Quiz quiz = new Quiz();
        quiz.statement = statement;
        quiz.answer = answer;
        quiz.explanation = explanation;
        quiz.sourceProductName = sourceProductName;
        quiz.sourceProductId = sourceProductId;
        quiz.quizDate = quizDate;
        return quiz;
    }

    public static Quiz createFrom(Quiz source, LocalDate newDate){
        Quiz quiz = new Quiz();
        quiz.statement = source.statement;
        quiz.answer = source.answer;
        quiz.explanation = source.explanation;
        quiz.sourceProductName = source.sourceProductName;
        quiz.sourceProductId = source.sourceProductId;
        quiz.quizDate = newDate;
        return quiz;
    }
}
