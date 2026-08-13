package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryDetailResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryResponse;
import com.naengpa.naengpamasterbackend.inquiry.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AdminInquiryRepository extends JpaRepository<Inquiry, Long> {

    // 삭제되지 않은 전체 미답변 문의 수를 조회합니다.
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.isAnswered = false AND i.isDeleted = false")
    Long countPendingInquiries();

    // 기준 시각보다 오래된 미답변 문의 수를 조회합니다.
    @Query("""
            SELECT COUNT(i)
            FROM Inquiry i
            WHERE i.isAnswered = false
              AND i.isDeleted = false
              AND i.createdAt < :cutoff
            """)
    Long countPendingInquiriesCreatedBefore(@Param("cutoff") LocalDateTime cutoff);

    // 문의·작성자·활성 답변을 조인해 관리자 문의 상세를 조회합니다.
    @Query("""
            SELECT new com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryDetailResponse(
                i.id,
                i.memberId,
                i.title,
                i.content,
                m.nickname,
                i.isAnswered,
                i.createdAt,
                a.id,
                a.content,
                a.createdBy,
                a.createdAt
            )
            FROM Inquiry i
            LEFT JOIN Member m ON m.id = i.memberId
            LEFT JOIN InquiryAnswer a ON a.inquiryId = i.id AND a.isDeleted = false
            WHERE i.id = :inquiryId
              AND i.isDeleted = false
            """)
    Optional<AdminInquiryDetailResponse> findInquiryDetail(
            @Param("inquiryId") Long inquiryId
    );

    // 삭제되지 않은 문의를 ID로 조회합니다.
    Optional<Inquiry> findByIdAndIsDeletedFalse(Long inquiryId);

    // 답변 여부에 맞는 문의와 작성자 닉네임을 페이지 단위로 조회합니다.
    @Query(
            value = """
                SELECT new com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryResponse(
                    i.id,
                    i.memberId,
                    m.nickname,
                    i.title,
                    i.isAnswered,
                    i.createdAt
                )
                FROM Inquiry i
                LEFT JOIN Member m ON m.id = i.memberId
                WHERE i.isAnswered = :isAnswered
                  AND i.isDeleted = false
                """,
            countQuery = """
                SELECT COUNT(i)
                FROM Inquiry i
                WHERE i.isAnswered = :isAnswered
                  AND i.isDeleted = false
                """
    )
    Page<AdminInquiryResponse> findInquiryList(
            @Param("isAnswered") Boolean isAnswered,
            Pageable pageable
    );
}
