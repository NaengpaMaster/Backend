package com.naengpa.naengpamasterbackend.quiz.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.quiz.dto.request.QuizSubmitRequest;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizSubmitResponse;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizTodayResponse;
import com.naengpa.naengpamasterbackend.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<QuizTodayResponse>> getTodayQUiz(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String email = userDetails.getUsername();

        return ResponseEntity.ok(
                ApiResponse.success("오늘의 퀴즈 조회에 성공했습니다.", quizService.getTodayQuiz(email))
        );
    }

    @PostMapping("/today/submit")
    public ResponseEntity<ApiResponse<QuizSubmitResponse>> submitQuiz(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody QuizSubmitRequest request
    ){
        String email = userDetails.getUsername();

        return ResponseEntity.ok(
          ApiResponse.success("퀴즈 제출에 성공했습니다.", quizService.submitQuiz(email, request))
        );
    }

}
