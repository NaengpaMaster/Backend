package com.naengpa.naengpamasterbackend.score.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.score.dto.response.ScoreByReasonResponse;
import com.naengpa.naengpamasterbackend.score.dto.response.ScoreHistoryResponse;
import com.naengpa.naengpamasterbackend.score.dto.response.ScoreResponse;
import com.naengpa.naengpamasterbackend.score.dto.response.ScoreSummaryResponse;
import com.naengpa.naengpamasterbackend.score.scheduler.DailyScoreScheduler;
import com.naengpa.naengpamasterbackend.score.service.ScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "냉파점수", description = "냉파 점수 조회 및 점수 분석 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scores")
public class ScoreController {

    private final ScoreService scoreService;

    @Operation(summary = "냉파 점수 조회", description = "로그인한 회원의 현재 냉파 점수를 조회한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<ScoreResponse>> getScores(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();

        return ResponseEntity.ok(
                ApiResponse.success("회원 점수 조회에 성공 했습니다.", scoreService.getScore(email))
        );
    }

    @Operation(summary = "냉파 점수 산정 내역 조회", description = "로그인한 회원의 점수 변동 이력을 페이지 단위로 조회한다.")
    @GetMapping("/histories")
    public ResponseEntity<ApiResponse<Page<ScoreHistoryResponse>>> getScoreHistories(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        String email = userDetails.getUsername();
        return ResponseEntity.ok(
                ApiResponse.success("회원 점수 산정 내역 조회에 성공 했습니다.", scoreService.getScoreHistories(email, pageable))
        );
    }

    @Operation(summary = "이번 달 사유별 점수 획득 현황 조회", description = "이번 달(1일~오늘) 기준, 점수 변동 사유별 발생 횟수와 합계 점수를 조회한다.")
    @GetMapping("/analysis/by-reason")
    public ResponseEntity<ApiResponse<List<ScoreByReasonResponse>>> getScoreReasons(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String email = userDetails.getUsername();

        return ResponseEntity.ok(
                ApiResponse.success("사유별 점수 현황 조회에 성공 했습니다.", scoreService.getScoreByReason(email))
        );
    }

    @Operation(summary = "이번 달 점수 변동 요약 조회", description = "이번 달(1일~오늘) 기준, 총 획득/총 감점/순변동을 조회한다.")
    @GetMapping("/analysis/summary")
    public ResponseEntity<ApiResponse<ScoreSummaryResponse>> getSummary(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String email = userDetails.getUsername();

        return ResponseEntity.ok(
          ApiResponse.success("점수 변동 요약 조회에 성공했습니다.", scoreService.getSummary(email))
        );
    }

    @Operation(summary = "이번 달 최대 영향 사유 조회", description = "이번 달(1일~오늘) 기준, 점수 변동 중 절댓값 기준으로 가장 큰 영향을 준 사유를 조회한다.")
    @GetMapping("/analysis/highlight")
    public ResponseEntity<ApiResponse<ScoreByReasonResponse>> getHighlight(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        String email = userDetails.getUsername();

        return ResponseEntity.ok(
                ApiResponse.success("최대 영향 사유 조회에 성공했습니다.", scoreService.getHighlight(email))
        );
    }

}
