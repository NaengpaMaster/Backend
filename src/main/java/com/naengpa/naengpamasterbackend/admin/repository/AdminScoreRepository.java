package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.admin.projection.ScoreAverageProjection;
import com.naengpa.naengpamasterbackend.score.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AdminScoreRepository extends JpaRepository<Score, Long> {

    // 냉파 점수 평균 조회
    @Query(value = """
            SELECT COALESCE(AVG(s.score), 0) AS "averageScore",
                   COUNT(*) AS "memberCount"
            FROM scores s
            JOIN members m ON m.member_id = s.member_id
            WHERE m.role = 'USER'
              AND m.status = 'ACTIVE'
              AND m.deleted_at IS NULL
              AND s.updated_at >= :startAt
              AND s.updated_at < :endExclusive
            """, nativeQuery = true)
    ScoreAverageProjection findScoreAverage(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

}
