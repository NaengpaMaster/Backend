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

import java.util.Optional;

@Repository
public interface AdminInquiryRepository extends JpaRepository<Inquiry, Long> {

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.isAnswered = false AND i.isDeleted = false")
    Long countPendingInquiries();

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

    Optional<Inquiry> findByIdAndIsDeletedFalse(Long inquiryId);

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
