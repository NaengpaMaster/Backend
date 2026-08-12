package com.naengpa.naengpamasterbackend.fridge.report.repository;

import com.naengpa.naengpamasterbackend.fridge.report.entity.WeeklyFridgeReportDeliveryLog;
import com.naengpa.naengpamasterbackend.fridge.report.entity.WeeklyFridgeReportDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyFridgeReportDeliveryLogRepository extends JpaRepository<WeeklyFridgeReportDeliveryLog, Long> {

    boolean existsByFridgeIdAndReceiverMemberIdAndReportWeekAndStatus(
            Long fridgeId,
            Long receiverMemberId,
            String reportWeek,
            WeeklyFridgeReportDeliveryStatus status
    );
}
