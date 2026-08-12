package com.naengpa.naengpamasterbackend.fridge.report.scheduler;

import com.naengpa.naengpamasterbackend.fridge.report.service.WeeklyFridgeReportDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyFridgeReportScheduler {

    private final WeeklyFridgeReportDispatchService dispatchService;

    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Seoul")
    public void sendWeeklyReports() {
        log.info("주간 냉장고 리포트 메일 스케줄러 시작");
        dispatchService.dispatchWeeklyReports();
    }
}
