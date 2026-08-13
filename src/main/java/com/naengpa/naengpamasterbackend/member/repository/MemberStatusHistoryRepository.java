package com.naengpa.naengpamasterbackend.member.repository;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatusHistoryResponse;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MemberStatusHistoryRepository extends JpaRepository<MemberStatusHistory, Long> {

    // 선택 기간의 회원 상태 변경 이력과 현재 회원 정보를 페이지 단위로 조회합니다.
    @Query(
            value = """
                    SELECT new com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatusHistoryResponse(
                        h.id,
                        h.memberId,
                        m.nickname,
                        m.email,
                        h.previousStatus,
                        h.changedStatus,
                        m.status,
                        h.createdAt
                    )
                    FROM MemberStatusHistory h
                    JOIN Member m ON m.id = h.memberId
                    WHERE h.createdAt >= :startAt
                      AND h.createdAt < :endExclusive
                    ORDER BY h.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(h)
                    FROM MemberStatusHistory h
                    JOIN Member m ON m.id = h.memberId
                    WHERE h.createdAt >= :startAt
                      AND h.createdAt < :endExclusive
                    """
    )
    Page<AdminMemberStatusHistoryResponse> findAdminStatusHistories(
            @Param("startAt") LocalDateTime startAt,
            @Param("endExclusive") LocalDateTime endExclusive,
            Pageable pageable
    );
}
