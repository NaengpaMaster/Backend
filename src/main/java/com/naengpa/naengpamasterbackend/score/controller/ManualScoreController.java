package com.naengpa.naengpamasterbackend.score.controller;

import com.naengpa.naengpamasterbackend.score.scheduler.DailyScoreScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "냉파점수-관리자", description = "관리자용 점수 스케줄러 수동 실행 API")
@RestController
@RequiredArgsConstructor
public class ManualScoreController {

    private final DailyScoreScheduler dailyScoreScheduler;

    @Operation(summary = "점수 스케줄러 수동 실행", description = "관리자가 일일 점수 스케줄러를 즉시 실행한다.")
    @PostMapping("/api/v1/admin/scores/run-scheduler")
    public ResponseEntity<String> runScoreScheduler() {
        dailyScoreScheduler.run();
        return ResponseEntity.ok("스케줄러 실행 완료");
    }
}
