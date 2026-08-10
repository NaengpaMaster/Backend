package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.admin.projection.DailyCountProjection;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminMemberRepository extends JpaRepository<Member, Long> {

    default Page<Member> findMembers(MemberRole role, MemberStatus status, String search, Pageable pageable) {
        if (status == MemberStatus.INACTIVE) {
            return findInactiveMembers(role, status, search, pageable);
        }
        return findActiveMembers(role, status, search, pageable);
    }

    @Query("SELECT m " +
            "FROM Member m " +
            "WHERE m.role = :role AND " +
            "m.status = :status AND " +
            "m.deletedAt IS NULL AND " +
            "(:search IS NULL OR m.nickname LIKE %:search% OR m.email LIKE %:search%)")
    Page<Member> findActiveMembers(
            @Param("role") MemberRole role,
            @Param("status") MemberStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT m " +
            "FROM Member m " +
            "WHERE m.role = :role AND " +
            "(m.status = :status OR m.deletedAt IS NOT NULL) AND " +
            "(:search IS NULL OR m.nickname LIKE %:search% OR m.email LIKE %:search%)")
    Page<Member> findInactiveMembers(
            @Param("role") MemberRole role,
            @Param("status") MemberStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    default Long countByStatusAndRole(MemberStatus status, MemberRole role) {
        if (status == MemberStatus.INACTIVE) {
            return countInactiveByRole(role, status);
        }
        return countActiveByRole(role, status);
    }

    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status AND m.role = :role AND m.deletedAt IS NULL")
    Long countActiveByRole(@Param("role") MemberRole role, @Param("status") MemberStatus status);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.role = :role AND (m.status = :status OR m.deletedAt IS NOT NULL)")
    Long countInactiveByRole(@Param("role") MemberRole role, @Param("status") MemberStatus status);

    @Query(
            value = """
                     SELECT CAST(DATE_TRUNC(CAST(:granularity AS TEXT), m.created_at) AS DATE) AS date,
                            COUNT(*) AS count
                     FROM members m
                     WHERE m.role = 'USER'
                       AND m.created_at >= :startAt
                       AND m.created_at < :endExclusive
                    GROUP BY 1
                    ORDER BY date
                    """,
            nativeQuery = true
    )
    List<DailyCountProjection> countDailyNewMembers(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("granularity") String granularity
    );

    @Query(
            value = """
                    SELECT CAST(DATE_TRUNC(CAST(:granularity AS TEXT), h.created_at) AS DATE) AS date,
                           COUNT(DISTINCT h.member_id) AS count
                    FROM member_status_histories h
                    JOIN members m ON m.member_id = h.member_id
                    WHERE m.role = 'USER'
                      AND h.previous_status = 'ACTIVE'
                      AND h.changed_status = 'INACTIVE'
                      AND h.created_at >= :startAt
                      AND h.created_at < :endExclusive
                    GROUP BY 1
                    ORDER BY date
                    """,
            nativeQuery = true
    )
    List<DailyCountProjection> countDailyInactiveMembers(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("granularity") String granularity
    );

    @Query(
            value = """
                    SELECT COUNT(DISTINCT h.member_id)
                    FROM member_status_histories h
                    JOIN members m ON m.member_id = h.member_id
                    WHERE m.role = 'USER'
                      AND h.previous_status = 'ACTIVE'
                      AND h.changed_status = 'INACTIVE'
                      AND h.created_at >= :startAt
                      AND h.created_at < :endExclusive
                    """,
            nativeQuery = true
    )
    long countInactiveMembers(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM members
            WHERE role = 'USER'
              AND created_at >= :startAt
              AND created_at < :endExclusive
            """, nativeQuery = true)
    long countNewMembers(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    Optional<Member> findByEmail(String adminEmail);

    int countByRoleAndStatus(MemberRole memberRole, MemberStatus memberStatus);
}
