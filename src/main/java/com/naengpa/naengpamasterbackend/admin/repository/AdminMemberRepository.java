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

    // 회원 상태에 따라 활성 또는 비활성 회원 조회 쿼리를 선택합니다.
    default Page<Member> findMembers(MemberRole role, MemberStatus status, String search, Pageable pageable) {
        if (status == MemberStatus.INACTIVE) {
            return findInactiveMembers(role, status, search, pageable);
        }
        return findActiveMembers(role, status, search, pageable);
    }

    // 역할·활성 상태·검색어에 맞는 삭제되지 않은 회원을 조회합니다.
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

    // 역할·비활성 상태·검색어에 맞는 비활성 또는 삭제 회원을 조회합니다.
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

    // 회원 상태에 따라 활성 또는 비활성 회원 집계 쿼리를 선택합니다.
    default Long countByStatusAndRole(MemberStatus status, MemberRole role) {
        if (status == MemberStatus.INACTIVE) {
            return countInactiveByRole(role, status);
        }
        return countActiveByRole(role, status);
    }

    // 역할과 상태가 일치하는 삭제되지 않은 활성 회원 수를 조회합니다.
    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status AND m.role = :role AND m.deletedAt IS NULL")
    Long countActiveByRole(@Param("role") MemberRole role, @Param("status") MemberStatus status);

    // 역할이 일치하는 비활성 또는 삭제 회원 수를 조회합니다.
    @Query("SELECT COUNT(m) FROM Member m WHERE m.role = :role AND (m.status = :status OR m.deletedAt IS NOT NULL)")
    Long countInactiveByRole(@Param("role") MemberRole role, @Param("status") MemberStatus status);

    // 선택 기간의 신규 가입 USER 수를 일·주·월 단위로 집계합니다.
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

    // 선택 기간의 비활성 처리 고유 USER 수를 일·주·월 단위로 집계합니다.
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

    // 선택 기간에 비활성 처리된 고유 USER 수를 조회합니다.
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

    // 선택 기간에 가입한 USER 수를 조회합니다.
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

    // 이메일로 회원을 조회합니다.
    Optional<Member> findByEmail(String adminEmail);

    // 역할과 상태가 일치하는 회원 수를 조회합니다.
    int countByRoleAndStatus(MemberRole memberRole, MemberStatus memberStatus);
}
