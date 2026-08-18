package com.naengpa.naengpamasterbackend.settlement.repository;

import com.naengpa.naengpamasterbackend.settlement.entity.MonthlySettlement;
import com.naengpa.naengpamasterbackend.settlement.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlySettlementRepository extends JpaRepository<MonthlySettlement, Long> {

    Optional<MonthlySettlement> findBySettlementMonth(String settlementMonth);

    List<MonthlySettlement> findAllByOrderBySettlementMonthDesc();

    List<MonthlySettlement> findByStatusOrderBySettlementMonthDesc(SettlementStatus status);

    List<MonthlySettlement> findBySettlementMonthOrderBySettlementMonthDesc(String settlementMonth);

    List<MonthlySettlement> findByStatusAndSettlementMonthOrderBySettlementMonthDesc(
            SettlementStatus status,
            String settlementMonth
    );
}
