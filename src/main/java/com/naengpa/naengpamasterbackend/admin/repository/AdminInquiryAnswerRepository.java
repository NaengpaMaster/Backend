package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.inquiry.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminInquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    Optional<InquiryAnswer> findByInquiryIdAndIsDeletedFalse(Long inquiryId);

    boolean existsByInquiryIdAndIsDeletedFalse(Long inquiryId);

    Optional<InquiryAnswer> findByIdAndInquiryIdAndIsDeletedFalse(
            Long answerId,
            Long inquiryId
    );
}
