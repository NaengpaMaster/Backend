package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.inquiry.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminInquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    // 문의에 연결된 삭제되지 않은 답변을 조회합니다.
    Optional<InquiryAnswer> findByInquiryIdAndIsDeletedFalse(Long inquiryId);

    // 문의에 삭제되지 않은 답변이 이미 존재하는지 확인합니다.
    boolean existsByInquiryIdAndIsDeletedFalse(Long inquiryId);

    // 문의 ID와 답변 ID가 모두 일치하는 삭제되지 않은 답변을 조회합니다.
    Optional<InquiryAnswer> findByIdAndInquiryIdAndIsDeletedFalse(
            Long answerId,
            Long inquiryId
    );
}
