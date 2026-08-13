package com.naengpa.naengpamasterbackend.quiz.controller;

import com.naengpa.naengpamasterbackend.quiz.scheduler.DailyQuizScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "퀴즈-관리자", description = "관리자용 퀴즈 스케줄러 수동 실행 API")
@RestController
@RequiredArgsConstructor
public class ManualQuizController {

    private final DailyQuizScheduler dailyQuizScheduler;

    @Operation(summary = "퀴즈 스케줄러 수동 실행", description = "관리자가 일일 퀴즈 생성 스케줄러를 즉시 실행한다.")
    @PostMapping("/api/v1/admin/quizzes/run-scheduler")
    public ResponseEntity<String> runQuizScheduler(HttpServletRequest request){
        String clientIp = request.getRemoteAddr();

        log.info("퀴즈 스케줄러 수동 실행 - IP: {}", clientIp);

        dailyQuizScheduler.generateDailyQuiz();
        return ResponseEntity.ok("퀴즈 스케줄러 실행 완료");
    }
}
