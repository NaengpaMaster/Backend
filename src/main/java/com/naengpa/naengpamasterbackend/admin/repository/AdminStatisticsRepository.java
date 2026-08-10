package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.admin.projection.*;
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

    // 삭제되지 않은 레시피를 등록 주체별로 조회
    @Query(value = """
            SELECT COUNT(*) AS "totalCount",
                   COUNT(*) FILTER (WHERE r.created_by IS NULL) AS "baseCount",
                   COUNT(*) FILTER (WHERE m.role = 'USER') AS "memberCount",
                   COUNT(*) FILTER (WHERE m.role = 'ADMIN') AS "adminCount"
            FROM recipes r
            LEFT JOIN members m ON m.member_id = r.created_by
            WHERE r.is_deleted = FALSE
            """, nativeQuery = true)
    RecipeCountProjection countRecipesByCreatorType();

    // 선택 기간 카테고리별 신규 레시피 수 조회
    @Query(value = """
            SELECT c.name AS "categoryName",
                   COUNT(1) AS "recipeCount",
                   COUNT(1) FILTER (WHERE r.created_by IS NULL) AS "baseRecipeCount",
                   COUNT(1) FILTER (WHERE m.role = 'USER') AS "memberRecipeCount",
                   COUNT(1) FILTER (WHERE m.role = 'ADMIN') AS "adminRecipeCount"
            FROM recipes r
            JOIN recipe_categories c ON c.recipe_category_id = r.recipe_category_id
            LEFT JOIN members m ON m.member_id = r.created_by
            WHERE r.is_deleted = FALSE
              AND r.created_at >= :startAt
              AND r.created_at < :endExclusive
            GROUP BY c.recipe_category_id, c.name
            ORDER BY "recipeCount" DESC, c.name
            """, nativeQuery = true)
    List<RecipeCategoryCountProjection> countRecipesByCategory(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    // 선택 기간 날짜별 재료 등록·만료 조회
    @Query(value = """
            SELECT CAST(days.date AS DATE) AS date,
                   COALESCE(registered.count, 0) AS "registeredCount",
                   COALESCE(expired.count, 0) AS "expiredCount"
            FROM generate_series(
                    DATE_TRUNC(CAST(:granularity AS TEXT), CAST(:startDate AS TIMESTAMP)),
                    DATE_TRUNC(CAST(:granularity AS TEXT), CAST(:endDate AS TIMESTAMP)),
                    CASE CAST(:granularity AS TEXT)
                        WHEN 'day' THEN INTERVAL '1 day'
                        WHEN 'week' THEN INTERVAL '1 week'
                        ELSE INTERVAL '1 month'
                    END
                 ) AS days(date)
            LEFT JOIN (
                SELECT CAST(DATE_TRUNC(CAST(:granularity AS TEXT), created_at) AS DATE) AS date,
                       COUNT(*) AS count
                FROM fridge_items
                WHERE created_at >= :startAt AND created_at < :endExclusive
                GROUP BY 1
            ) registered ON registered.date = CAST(days.date AS DATE)
            LEFT JOIN (
                SELECT CAST(DATE_TRUNC(CAST(:granularity AS TEXT), created_at) AS DATE) AS date,
                       COUNT(*) AS count
                FROM expired_products
                WHERE created_at >= :startDate AND created_at <= :endDate
                GROUP BY 1
            ) expired ON expired.date = CAST(days.date AS DATE)
            ORDER BY date
            """, nativeQuery = true)
    List<DailyMaterialStatisticsProjection> findDailyMaterialStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("granularity") String granularity
    );

    // 선택 기간 등록 재료 수 조회
    @Query(value = """
            SELECT COUNT(*)
            FROM fridge_items
            WHERE created_at >= :startAt
              AND created_at < :endExclusive
            """, nativeQuery = true)
    Long countRegisteredIngredients(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    // 선택 기간 신규 레시피 수 조회
    @Query(value = """
            SELECT COUNT(*)
            FROM recipes
            WHERE created_at >= :startAt
              AND created_at < :endExclusive
            """, nativeQuery = true)
    Long countCreatedRecipes(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    // 선택 기간 유통기한 만료 건수 조회
    @Query("SELECT COUNT(ep) FROM ExpiredProduct ep WHERE ep.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 카테고리별 만료량 조회
    @Query("""
            SELECT ep.categoryName AS categoryName, COUNT(ep) AS expiredCount
            FROM ExpiredProduct ep
            WHERE ep.createdAt BETWEEN :startDate AND :endDate
            GROUP BY ep.categoryName
            ORDER BY COUNT(ep) DESC
            """)
    List<CategoryStatProjection> findExpiredCountByCategory(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // top 5 만료 재료 조회 (7일)
    @Query("""
            SELECT ep.productName AS productName, COUNT(ep) AS expiredCount
            FROM ExpiredProduct ep
            WHERE ep.createdAt BETWEEN :startDate AND :endDate
            GROUP BY ep.productName
            ORDER BY COUNT(ep) DESC
            """)
    List<ExpiredIngredientProjection> findTop5ExpiredIngredientsBetween(
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
            SELECT 'fridge' AS service,
                   CAST(DATE_TRUNC(CAST(:granularity AS TEXT), f.created_at) AS DATE) AS date,
                   COUNT(DISTINCT f.member_id) AS count
            FROM fridge_items f
            JOIN members m ON m.member_id = f.member_id
            WHERE m.role = 'USER' AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
              AND f.created_at >= :startAt AND f.created_at < :endExclusive
            GROUP BY 1, 2
            UNION ALL
            SELECT 'shopping' AS service,
                   CAST(DATE_TRUNC(CAST(:granularity AS TEXT), s.created_at) AS DATE) AS date,
                   COUNT(DISTINCT s.member_id) AS count
            FROM shopping_items s
            JOIN members m ON m.member_id = s.member_id
            WHERE m.role = 'USER' AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
              AND s.created_at >= :startAt AND s.created_at < :endExclusive
            GROUP BY 1, 2
            UNION ALL
            SELECT 'recipe' AS service,
                   CAST(DATE_TRUNC(CAST(:granularity AS TEXT), r.created_at) AS DATE) AS date,
                   COUNT(DISTINCT r.created_by) AS count
            FROM recipes r
            JOIN members m ON m.member_id = r.created_by
            WHERE m.role = 'USER' AND m.status = 'ACTIVE' AND m.deleted_at IS NULL
              AND r.created_at >= :startAt AND r.created_at < :endExclusive
            GROUP BY 1, 2
            ORDER BY service, date
            """, nativeQuery = true)
    List<DailyServiceUsageProjection> countDailyServiceUsageMembers(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("granularity") String granularity
    );
}
