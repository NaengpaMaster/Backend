package com.naengpa.naengpamasterbackend.agent.usage.repository;

import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmUsageLog;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmFeatureType;
import com.naengpa.naengpamasterbackend.agent.usage.entity.LlmCallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LlmUsageLogRepository extends JpaRepository<LlmUsageLog, Long> {

    List<LlmUsageLog> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<LlmUsageLog> findAllByOrderByCreatedAtDesc();

    Page<LlmUsageLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<LlmUsageLog> findByFeatureTypeOrderByCreatedAtDesc(LlmFeatureType featureType, Pageable pageable);

    @Query("""
            SELECT COUNT(l) AS totalCount,
                   COALESCE(SUM(CASE WHEN l.status = :successStatus THEN 1 ELSE 0 END), 0) AS successCount,
                   COALESCE(SUM(CASE WHEN l.status = :failedStatus THEN 1 ELSE 0 END), 0) AS failedCount,
                   COALESCE(SUM(l.totalTokens), 0) AS totalTokens,
                   COALESCE(SUM(l.estimatedCost), 0) AS totalEstimatedCost
            FROM LlmUsageLog l
            WHERE :featureType IS NULL OR l.featureType = :featureType
            """)
    LlmUsageSummaryProjection summarize(
            @Param("featureType") LlmFeatureType featureType,
            @Param("successStatus") LlmCallStatus successStatus,
            @Param("failedStatus") LlmCallStatus failedStatus
    );
}
