package com.naengpa.naengpamasterbackend.fridge.report.service;

import com.naengpa.naengpamasterbackend.fridge.report.dto.WeeklyFridgeReportRecipient;
import com.naengpa.naengpamasterbackend.fridge.report.repository.WeeklyFridgeReportRecipientProjection;
import com.naengpa.naengpamasterbackend.fridge.report.repository.WeeklyFridgeReportRecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyFridgeReportRecipientService {

    private final WeeklyFridgeReportRecipientRepository recipientRepository;

    @Transactional(readOnly = true)
    public List<WeeklyFridgeReportRecipient> findRecipients() {
        return recipientRepository.findWeeklyReportRecipients()
                .stream()
                .map(this::toRecipient)
                .distinct()
                .toList();
    }

    private WeeklyFridgeReportRecipient toRecipient(WeeklyFridgeReportRecipientProjection projection) {
        return new WeeklyFridgeReportRecipient(
                projection.getFridgeId(),
                projection.getFridgeName(),
                projection.getReceiverMemberId(),
                projection.getReceiverEmail()
        );
    }
}
