package com.naengpa.naengpamasterbackend.receipt.repository;

import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiptAnalysisItemRepository extends JpaRepository<ReceiptAnalysisItem, Long> {

    List<ReceiptAnalysisItem> findByReceiptAnalysisIdOrderByCreatedAtAsc(Long receiptAnalysisId);
}