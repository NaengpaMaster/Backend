package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberRoleRequest;
import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberStatusRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberDetailResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminMemberService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
@Tag(name = "관리자 회원 관리", description = "관리자 회원 목록/상세 조회, 회원 상태 및 권한 변경 API")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 회원 목록 조회 API(관리자 목록, 회원 목록, 탈퇴 회원 목록)
    @Operation(summary = "관리자 회원 목록 조회", description = "관리자가 역할과 상태 기준으로 회원 목록을 조회합니다. 검색어가 있으면 이메일/닉네임 기준으로 필터링합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminMemberResponse>>> getMembers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "조회할 회원 역할") @RequestParam MemberRole role,
            @Parameter(description = "조회할 회원 상태") @RequestParam MemberStatus status,
            @Parameter(description = "이메일/닉네임 검색어") @RequestParam(required = false) String search) {

        return ResponseEntity.ok(ApiResponse.success(adminMemberService.getMembers(role, status, search, pageable)));
    }

    // 회원 상세 조회
    @Operation(summary = "관리자 회원 상세 조회", description = "관리자가 특정 회원의 상세 정보와 프로필/냉파 점수 정보를 조회합니다.")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<AdminMemberDetailResponse>> getMemberDetail(
            @Parameter(description = "조회할 회원 ID") @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(adminMemberService.getMemberDetail(memberId)));
    }

    // 회원 status 변경
    @Operation(summary = "회원 상태 변경", description = "관리자가 회원을 활성/비활성 상태로 변경합니다. 회원 탈퇴/복구 정책은 상태 변경 방식으로 처리합니다.")
    @PatchMapping("/{memberId}/status")
    public ResponseEntity<ApiResponse<Void>> updateMemberStatus(
            @Parameter(description = "상태를 변경할 회원 ID") @PathVariable Long memberId,
            @RequestBody @Valid AdminMemberStatusRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();

        adminMemberService.updateMemberStatus(memberId, request, adminEmail);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회원 role 변경
    @Operation(summary = "회원 권한 변경", description = "관리자가 회원 권한을 USER 또는 ADMIN으로 변경합니다. 자기 자신의 관리자 권한 해제와 마지막 관리자 해제는 차단됩니다.")
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @Parameter(description = "권한을 변경할 회원 ID") @PathVariable Long memberId,
            @RequestBody @Valid AdminMemberRoleRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();

        adminMemberService.updateMemberRole(memberId, request, adminEmail);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
