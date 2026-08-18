package com.naengpa.naengpamasterbackend.settlement.repository;

import com.naengpa.naengpamasterbackend.settlement.entity.SettlementPaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementPaymentDetailRepository extends JpaRepository<SettlementPaymentDetail, Long> {

    void deleteAllByMonthlySettlementId(Long monthlySettlementId);

    List<SettlementPaymentDetail> findByMonthlySettlementIdOrderBySettlementPaymentDetailIdAsc(
            Long monthlySettlementId
    );
}
