package com.naengpa.naengpamasterbackend.receipt.repository;

import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptAnalysisRepository extends JpaRepository<ReceiptAnalysis, Long> {

    // 본인 영수증인지 확인하면서 조회하기 위해
    Optional<ReceiptAnalysis> findByReceiptAnalysisIdAndMemberId(
            Long receiptAnalysisId,
            Long memberId
    );
}