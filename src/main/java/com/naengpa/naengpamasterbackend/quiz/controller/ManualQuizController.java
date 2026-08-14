package com.naengpa.naengpamasterbackend.quiz.controller;

import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.quiz.scheduler.DailyQuizScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "퀴즈-관리자", description = "관리자용 퀴즈 스케줄러 수동 실행 API")
@RestController
@RequiredArgsConstructor
public class ManualQuizController {

    private final DailyQuizScheduler dailyQuizScheduler;
    private final MemberRepository memberRepository;

    @Operation(summary = "퀴즈 스케줄러 수동 실행", description = "관리자가 일일 퀴즈 생성 스케줄러를 즉시 실행한다.")
    @PostMapping("/api/v1/admin/quizzes/run-scheduler")
    public ResponseEntity<String> runQuizScheduler(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ){
        String clientIp = request.getRemoteAddr();

        Member member = memberRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        log.info("퀴즈 스케줄러 수동 실행 - memberId: {}, IP: {}", member.getId(), clientIp);

        dailyQuizScheduler.generateDailyQuizManually(member.getId());
        return ResponseEntity.ok("퀴즈 스케줄러 실행 완료");
    }
}
