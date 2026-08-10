package com.naengpa.naengpamasterbackend.quiz.entitiy;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_result_id")
    private Long quizResultId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "submitted_answer", nullable = false)
    private Boolean submittedAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    void prePersist() {
        this.submittedAt = LocalDateTime.now();
    }

    public static QuizResult create(Long memberId, Long quizId, Boolean submittedAnswer, Boolean isCorrect) {
        QuizResult result = new QuizResult();
        result.memberId = memberId;
        result.quizId = quizId;
        result.submittedAnswer = submittedAnswer;
        result.isCorrect = isCorrect;
        return result;
    }
}
