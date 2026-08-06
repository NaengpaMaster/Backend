package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.admin.projection.DailyServiceUsageProjection;
import com.naengpa.naengpamasterbackend.admin.projection.ServiceUsageCountProjection;
import com.naengpa.naengpamasterbackend.statistics.entity.ExpiredProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminStatisticsRepository extends JpaRepository<ExpiredProduct, Long> {

    // 유통기한 만료 건수 조회 // 주간 만료 추이 (날짜별 만료 건수)
    @Query("SELECT COUNT(ep) FROM ExpiredProduct ep WHERE ep.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 카테고리별 만료량 조회
    @Query("SELECT ep.categoryName, COUNT(ep) FROM ExpiredProduct ep " +
            "WHERE ep.createdAt >= :startDate " +
            "GROUP BY ep.categoryName ORDER BY COUNT(ep) DESC")
    List<Object[]> findExpiredCountByCategory(@Param("startDate") LocalDate startDate);

    // top 5 만료 재료 조회 (7일)
    @Query("SELECT ep.productName, COUNT(ep) as cnt FROM ExpiredProduct ep WHERE ep.createdAt BETWEEN :startDate AND :endDate GROUP BY ep.productName ORDER BY cnt DESC")
    List<Object[]> findTop5ExpiredIngredientsBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // 선택 기간 고유 이용 회원 수
    @Query(value = """
            SELECT 'fridge' AS service, COUNT(DISTINCT f.member_id) AS count
            FROM fridge_items f
            JOIN members m ON m.member_id = f.member_id
            WHERE m.role = 'USER' AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
              AND f.created_at >= :startAt AND f.created_at < :endExclusive
            UNION ALL
            SELECT 'shopping' AS service, COUNT(DISTINCT s.member_id) AS count
            FROM shopping_items s
            JOIN members m ON m.member_id = s.member_id
            WHERE m.role = 'USER' AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
              AND s.created_at >= :startAt AND s.created_at < :endExclusive
            UNION ALL
            SELECT 'recipe' AS service, COUNT(DISTINCT r.created_by) AS count
            FROM recipes r
            JOIN members m ON m.member_id = r.created_by
            WHERE m.role = 'USER' AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
              AND r.created_at >= :startAt AND r.created_at < :endExclusive
            """, nativeQuery = true)
    List<ServiceUsageCountProjection> countServiceUsageMembers(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    // 날짜 별 고유 이용 회원 수
    @Query(value = """
            SELECT 'fridge' AS service, CAST(f.created_at AS DATE) AS date,
                   COUNT(DISTINCT f.member_id) AS count
            FROM fridge_items f
            JOIN members m ON m.member_id = f.member_id
            WHERE m.role = 'USER' AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
              AND f.created_at >= :startAt AND f.created_at < :endExclusive
            GROUP BY CAST(f.created_at AS DATE)
            UNION ALL
            SELECT 'shopping' AS service, CAST(s.created_at AS DATE) AS date,
                   COUNT(DISTINCT s.member_id) AS count
            FROM shopping_items s
            JOIN members m ON m.member_id = s.member_id
            WHERE m.role = 'USER' AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
              AND s.created_at >= :startAt AND s.created_at < :endExclusive
            GROUP BY CAST(s.created_at AS DATE)
            UNION ALL
            SELECT 'recipe' AS service, CAST(r.created_at AS DATE) AS date,
                   COUNT(DISTINCT r.created_by) AS count
            FROM recipes r
            JOIN members m ON m.member_id = r.created_by
            WHERE m.role = 'USER' AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
              AND r.created_at >= :startAt AND r.created_at < :endExclusive
            GROUP BY CAST(r.created_at AS DATE)
            ORDER BY service, date
            """, nativeQuery = true)
    List<DailyServiceUsageProjection> countDailyServiceUsageMembers(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

}
