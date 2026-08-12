package com.naengpa.naengpamasterbackend.quiz.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.quiz.dto.request.QuizSubmitRequest;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizSubmitResponse;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizTodayResponse;
import com.naengpa.naengpamasterbackend.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "퀴즈", description = "오늘의 퀴즈 조회 및 제출 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "오늘의 퀴즈 조회",
            description = "오늘 생성된 퀴즈를 조회한다. 정답(answer)은 응답에 포함되지 않는다. " +
                    "이미 제출한 경우, 선택했던 답과 정답 여부, 해설이 함께 응답된다.")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<QuizTodayResponse>> getTodayQUiz(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String email = userDetails.getUsername();

        return ResponseEntity.ok(
                ApiResponse.success("오늘의 퀴즈 조회에 성공했습니다.", quizService.getTodayQuiz(email))
        );
    }

    @Operation(summary = "오늘의 퀴즈 제출",
            description = "O/X 답변을 제출하고 정답 여부와 해설을 받는다. 이미 제출한 경우 409를 반환한다.")
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
