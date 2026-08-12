package com.naengpa.naengpamasterbackend.quiz.controller;

import com.naengpa.naengpamasterbackend.quiz.scheduler.DailyQuizScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ManualQuizController {

    private final DailyQuizScheduler dailyQuizScheduler;

    @PostMapping("/api/v1/admin/quizzes/run-scheduler")
    public ResponseEntity<String> runQuizScheduler(){
        dailyQuizScheduler.generateDailyQuiz();
        return ResponseEntity.ok("퀴즈 스케줄러 실행 완료");
    }
}
