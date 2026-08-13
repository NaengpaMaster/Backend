package com.naengpa.naengpamasterbackend.inquiry.repository;

import com.naengpa.naengpamasterbackend.inquiry.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 회원이 작성한 삭제되지 않은 문의를 최신순으로 조회합니다.
    Page<Inquiry> findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 삭제되지 않은 문의를 ID로 조회합니다.
    Optional<Inquiry> findByIdAndIsDeletedFalse(Long inquiryId);

    // 회원이 소유한 삭제되지 않은 문의를 조회합니다.
    Optional<Inquiry> findByIdAndMemberIdAndIsDeletedFalse(
            Long inquiryId,
            Long memberId
    );
}
