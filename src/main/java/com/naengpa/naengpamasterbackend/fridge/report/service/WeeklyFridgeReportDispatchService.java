package com.naengpa.naengpamasterbackend.fridge.report.service;

import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyFridgeReportRecipient;
import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyFridgeReportSummary;
import com.naengpa.naengpamasterbackend.fridge.report.entity.WeeklyFridgeReportDeliveryLog;
import com.naengpa.naengpamasterbackend.fridge.report.entity.WeeklyFridgeReportDeliveryStatus;
import com.naengpa.naengpamasterbackend.fridge.report.repository.WeeklyFridgeReportDeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyFridgeReportDispatchService {

    private final WeeklyFridgeReportRecipientService recipientService;
    private final WeeklyFridgeReportAggregationService aggregationService;
    private final WeeklyFridgeReportMailService mailService;
    private final WeeklyFridgeReportDeliveryLogRepository deliveryLogRepository;
    private final Clock clock;

    @Transactional
    public int dispatchWeeklyReports() {
        return dispatchWeeklyReports(false);
    }

    @Transactional
    public int dispatchWeeklyReports(boolean force) {
        LocalDate endDate = force ? LocalDate.now(clock) : LocalDate.now(clock).minusDays(1);
        String reportWeek = reportWeek(endDate);
        List<WeeklyFridgeReportRecipient> recipients = recipientService.findRecipients();
        Map<Long, List<WeeklyFridgeReportRecipient>> recipientsByMember = groupByMember(recipients);
        int sentCount = 0;

        for (Map.Entry<Long, List<WeeklyFridgeReportRecipient>> entry : recipientsByMember.entrySet()) {
            List<WeeklyFridgeReportRecipient> pendingRecipients = filterPendingRecipients(entry.getValue(), reportWeek, force);
            if (pendingRecipients.isEmpty()) {
                continue;
            }

            WeeklyFridgeReportRecipient representative = pendingRecipients.getFirst();
            List<WeeklyFridgeReportSummary> summaries = pendingRecipients.stream()
                    .map(recipient -> aggregationService.aggregate(recipient.fridgeId(), endDate))
                    .toList();
            try {
                mailService.send(representative.receiverEmail(), summaries);
                saveSuccessLogs(pendingRecipients, reportWeek);
                sentCount++;
            } catch (RuntimeException exception) {
                log.warn(
                        "주간 냉장고 리포트 메일 발송 실패 - receiverMemberId: {}, message: {}",
                        representative.receiverMemberId(),
                        exception.getMessage()
                );
                saveFailureLogs(pendingRecipients, reportWeek, exception.getMessage());
            }
        }

        log.info("주간 냉장고 리포트 메일 발송 완료 - 대상회원: {}, 발송: {}, 강제발송: {}", recipientsByMember.size(), sentCount, force);
        return sentCount;
    }

    private Map<Long, List<WeeklyFridgeReportRecipient>> groupByMember(List<WeeklyFridgeReportRecipient> recipients) {
        Map<Long, List<WeeklyFridgeReportRecipient>> recipientsByMember = new LinkedHashMap<>();
        for (WeeklyFridgeReportRecipient recipient : recipients) {
            recipientsByMember.computeIfAbsent(recipient.receiverMemberId(), ignored -> new ArrayList<>())
                    .add(recipient);
        }
        return recipientsByMember;
    }

    private List<WeeklyFridgeReportRecipient> filterPendingRecipients(
            List<WeeklyFridgeReportRecipient> recipients,
            String reportWeek,
            boolean force
    ) {
        if (force) {
            return recipients;
        }
        return recipients.stream()
                .filter(recipient -> !deliveryLogRepository.existsByFridgeIdAndReceiverMemberIdAndReportWeekAndStatus(
                        recipient.fridgeId(),
                        recipient.receiverMemberId(),
                        reportWeek,
                        WeeklyFridgeReportDeliveryStatus.SUCCESS
                ))
                .toList();
    }

    private void saveSuccessLogs(List<WeeklyFridgeReportRecipient> recipients, String reportWeek) {
        recipients.forEach(recipient -> {
            boolean alreadySucceeded = deliveryLogRepository.existsByFridgeIdAndReceiverMemberIdAndReportWeekAndStatus(
                    recipient.fridgeId(),
                    recipient.receiverMemberId(),
                    reportWeek,
                    WeeklyFridgeReportDeliveryStatus.SUCCESS
            );
            if (alreadySucceeded) {
                return;
            }
            deliveryLogRepository.save(WeeklyFridgeReportDeliveryLog.success(
                    recipient.fridgeId(),
                    recipient.receiverMemberId(),
                    recipient.receiverEmail(),
                    reportWeek
            ));
        });
    }

    private void saveFailureLogs(List<WeeklyFridgeReportRecipient> recipients, String reportWeek, String errorMessage) {
        recipients.forEach(recipient -> deliveryLogRepository.save(WeeklyFridgeReportDeliveryLog.failed(
                recipient.fridgeId(),
                recipient.receiverMemberId(),
                recipient.receiverEmail(),
                reportWeek,
                errorMessage
        )));
    }

    private String reportWeek(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.KOREA);
        int weekBasedYear = date.get(weekFields.weekBasedYear());
        int weekOfYear = date.get(weekFields.weekOfWeekBasedYear());
        return "%04d-W%02d".formatted(weekBasedYear, weekOfYear);
    }
}
