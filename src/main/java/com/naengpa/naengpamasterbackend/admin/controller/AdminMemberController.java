package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberRoleRequest;
import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberStatusRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberDetailResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminMemberService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "관리자 회원", description = "관리자 회원 조회 및 상태·권한 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 회원 목록 조회 API(관리자 목록, 회원 목록, 탈퇴 회원 목록)
    @Operation(summary = "회원 목록 조회", description = "역할과 상태를 기준으로 회원을 검색하고 페이지 단위로 조회합니다. 관리자 권한이 필요합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminMemberResponse>>> getMembers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "회원 역할", example = "USER") @RequestParam MemberRole role,
            @Parameter(description = "회원 상태", example = "ACTIVE") @RequestParam MemberStatus status,
            @Parameter(description = "닉네임 또는 이메일 검색어", example = "naengpa") @RequestParam(required = false) String search) {

        return ResponseEntity.ok(ApiResponse.success(adminMemberService.getMembers(role, status, search, pageable)));
    }

    // 회원 상세 조회
    @Operation(summary = "회원 상세 조회", description = "회원의 기본 정보와 서비스 이용 정보를 조회합니다. 관리자 권한이 필요합니다.")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<AdminMemberDetailResponse>> getMemberDetail(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(adminMemberService.getMemberDetail(memberId)));
    }

    // 회원 status 변경
    @Operation(summary = "회원 상태 변경", description = "회원을 활성 또는 비활성 상태로 변경합니다. 관리자 권한이 필요합니다.")
    @PatchMapping("/{memberId}/status")
    public ResponseEntity<ApiResponse<Void>> updateMemberStatus(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId,
            @RequestBody @Valid AdminMemberStatusRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();

        adminMemberService.updateMemberStatus(memberId, request, adminEmail);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회원 role 변경
    @Operation(summary = "회원 역할 변경", description = "회원 역할을 USER 또는 ADMIN으로 변경합니다. 관리자 권한이 필요합니다.")
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId,
            @RequestBody @Valid AdminMemberRoleRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();

        adminMemberService.updateMemberRole(memberId, request, adminEmail);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
