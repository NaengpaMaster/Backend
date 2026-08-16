package com.naengpa.naengpamasterbackend.global.auth.controller;

import com.naengpa.naengpamasterbackend.global.auth.dto.EmailAvailabilityResponse;
import com.naengpa.naengpamasterbackend.global.auth.dto.MemberResponse;
import com.naengpa.naengpamasterbackend.global.auth.dto.ProfileUpdateRequest;
import com.naengpa.naengpamasterbackend.global.auth.dto.SignUpRequest;
import com.naengpa.naengpamasterbackend.global.auth.service.AuthService;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateEmailException;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Validated
@Tag(name = "회원/프로필", description = "회원가입, 이메일 중복 확인, 내 정보 및 프로필 관리 API")
public class MemberAuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일/비밀번호로 회원가입합니다. 랜덤 닉네임이 자동 배정되며 탈퇴 회원과 동일 이메일은 재가입할 수 없습니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signup(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", authService.signup(request)));
    }

    @Operation(summary = "이메일 중복 확인", description = "회원가입에 사용할 이메일의 사용 가능 여부를 확인합니다.")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<EmailAvailabilityResponse>> checkEmail(
            @Parameter(description = "중복 확인할 이메일") @RequestParam @Email String email
    ) {
        boolean available = authService.isEmailAvailable(email);
        if (!available) {
            throw new DuplicateEmailException();
        }
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일입니다.", new EmailAvailabilityResponse(true)));
    }

    @Operation(summary = "내 회원 정보 조회", description = "로그인한 회원의 기본 회원 정보와 권한 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> me(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.getMember(authentication.getName())));
    }

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 가구 유형, 선호 음식, 못 먹는 재료를 포함한 프로필 정보를 조회합니다.")
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<MemberResponse>> profile(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.getMember(authentication.getName())));
    }

    @Operation(summary = "내 프로필 수정", description = "로그인한 회원의 가구 유형, 선호 음식, 못 먹는 재료 설정을 저장합니다.")
    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<MemberResponse>> updateProfile(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("프로필이 저장되었습니다.", authService.updateProfile(authentication.getName(), request)));
    }

    @Operation(summary = "회원탈퇴", description = "로그인한 회원을 탈퇴 처리합니다. 구독 중인 회원은 구독 취소 후 탈퇴할 수 있습니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @Parameter(hidden = true) Authentication authentication
    ) {
        authService.withdraw(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("회원탈퇴가 완료되었습니다.", null));
    }
}
