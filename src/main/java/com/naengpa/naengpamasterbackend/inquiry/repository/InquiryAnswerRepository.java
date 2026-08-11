package com.naengpa.naengpamasterbackend.inquiry.repository;

import com.naengpa.naengpamasterbackend.inquiry.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    // 문의에 연결된 삭제되지 않은 답변을 조회합니다.
    Optional<InquiryAnswer> findByInquiryIdAndIsDeletedFalse(Long inquiryId);

}
