package com.naengpa.naengpamasterbackend.score.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "score_histories")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_history_id")
    private Long scoreHistoryId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_reason", length = 100)
    private ScoreReason scoreReason;

    @Column(name = "target_name", length = 100)
    private String targetName;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "product_category_id")
    private Long productCategoryId;

    @Column(name = "score_delta", nullable = false)
    private Integer scoreDelta;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public static ScoreHistory create(Long memberId, ScoreReason reason, String targetName, Long targetId, Long productCategoryId, Integer delta){
        ScoreHistory history = new ScoreHistory();
        history.memberId = memberId;
        history.scoreReason = reason;
        history.targetName = targetName;
        history.targetId = targetId;
        history.productCategoryId = productCategoryId;
        history.scoreDelta = delta;
        return history;
    }

    public static ScoreHistory createSignupBonus(Long memberId){
        ScoreHistory history = new ScoreHistory();
        history.memberId = memberId;
        history.scoreReason = ScoreReason.SIGNUP_BONUS;
        history.scoreDelta = Score.INITIAL_SCORE;
        return history;
    }

}