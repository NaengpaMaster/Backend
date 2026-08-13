package com.naengpa.naengpamasterbackend.score.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "scores")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Score {

    static final int INITIAL_SCORE = 10;
    private static final int MAX_SCORE = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Long scoreId;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "score")
    private int score;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateScore(int newScore){
        this.score = newScore;
        this.updatedAt = LocalDateTime.now();
    }

    public static Score createInitial(Long memberId) {
        Score score = new Score();
        score.memberId = memberId;
        score.score = INITIAL_SCORE;
        score.updatedAt = LocalDateTime.now();
        return score;
    }

    public int addScore(int delta) {
        int before = this.score;
        this.score = Math.min(this.score + delta, MAX_SCORE);
        this.updatedAt = LocalDateTime.now();
        return this.score - before;
    }
}
