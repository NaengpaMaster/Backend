package com.naengpa.naengpamasterbackend.score.repository;

import com.naengpa.naengpamasterbackend.score.dto.response.ScoreByReasonResponse;
import com.naengpa.naengpamasterbackend.score.dto.response.ScoreSummaryResponse;
import com.naengpa.naengpamasterbackend.score.entity.ScoreHistory;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScoreAnalysisRepository extends JpaRepository <ScoreHistory, Long> {

    //사유별 점수 획득 현황
    @Query("""
            SELECT new com.naengpa.naengpamasterbackend.score.dto.response.ScoreByReasonResponse(
                        CAST(sh.scoreReason AS string), COUNT(sh), SUM(sh.scoreDelta) 
            )
            FROM ScoreHistory sh
            WHERE sh.memberId = :memberId
            AND sh.createdAt >= :startOfMonth
            GROUP BY sh.scoreReason
            """)
    List<ScoreByReasonResponse> findScoreSummaryByReason(
            @Param("memberId") Long memberId,
            @Param("startOfMonth") LocalDateTime startOfMonth);

    //이번 달 점수 변동 요약
    @Query("""
            SELECT new com.naengpa.naengpamasterbackend.score.dto.response.ScoreSummaryResponse(
                      SUM(CASE WHEN sh.scoreDelta > 0 THEN sh.scoreDelta ELSE 0 END),
                      SUM(CASE WHEN sh.scoreDelta < 0 THEN sh.scoreDelta ELSE 0 END),
                      SUM(sh.scoreDelta)
           )
           FROM ScoreHistory sh
           WHERE sh.memberId = :memberId
           AND sh.createdAt >= :startOfMonth
          """)
    ScoreSummaryResponse findSummary(
            @Param("memberId") Long memberId,
            @Param("startOfMonth")LocalDateTime startOfMonth);

}
