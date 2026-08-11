package com.naengpa.naengpamasterbackend.fridge.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_fridge_report_delivery_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyFridgeReportDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_fridge_report_delivery_log_id")
    private Long weeklyFridgeReportDeliveryLogId;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "receiver_member_id", nullable = false)
    private Long receiverMemberId;

    @Column(name = "receiver_email", nullable = false, length = 100)
    private String receiverEmail;

    @Column(name = "report_week", nullable = false, length = 10)
    private String reportWeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WeeklyFridgeReportDeliveryStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static WeeklyFridgeReportDeliveryLog success(
            Long fridgeId,
            Long receiverMemberId,
            String receiverEmail,
            String reportWeek
    ) {
        WeeklyFridgeReportDeliveryLog log = new WeeklyFridgeReportDeliveryLog();
        log.fridgeId = fridgeId;
        log.receiverMemberId = receiverMemberId;
        log.receiverEmail = receiverEmail;
        log.reportWeek = reportWeek;
        log.status = WeeklyFridgeReportDeliveryStatus.SUCCESS;
        log.sentAt = LocalDateTime.now();
        return log;
    }

    public static WeeklyFridgeReportDeliveryLog failed(
            Long fridgeId,
            Long receiverMemberId,
            String receiverEmail,
            String reportWeek,
            String errorMessage
    ) {
        WeeklyFridgeReportDeliveryLog log = new WeeklyFridgeReportDeliveryLog();
        log.fridgeId = fridgeId;
        log.receiverMemberId = receiverMemberId;
        log.receiverEmail = receiverEmail;
        log.reportWeek = reportWeek;
        log.status = WeeklyFridgeReportDeliveryStatus.FAILED;
        log.errorMessage = errorMessage;
        return log;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
