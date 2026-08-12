package com.naengpa.naengpamasterbackend.quiz.controller;

import com.naengpa.naengpamasterbackend.quiz.scheduler.DailyQuizScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "퀴즈-관리자", description = "관리자용 퀴즈 스케줄러 수동 실행 API")
@RestController
@RequiredArgsConstructor
public class ManualQuizController {

    private final DailyQuizScheduler dailyQuizScheduler;

    @Operation(summary = "퀴즈 스케줄러 수동 실행", description = "관리자가 일일 퀴즈 생성 스케줄러를 즉시 실행한다.")
    @PostMapping("/api/v1/admin/quizzes/run-scheduler")
    public ResponseEntity<String> runQuizScheduler(){
        dailyQuizScheduler.generateDailyQuiz();
        return ResponseEntity.ok("퀴즈 스케줄러 실행 완료");
    }
}
