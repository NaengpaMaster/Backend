package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberRoleRequest;
import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberStatusRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminMemberService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
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
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 회원 목록 조회 API(관리자 목록, 회원 목록, 탈퇴 회원 목록)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminMemberResponse>>> getMembers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam MemberRole role,
            @RequestParam MemberStatus status,
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(ApiResponse.success(adminMemberService.getMembers(role, status, search, pageable)));
    }

    // 회원 status 변경
    @PatchMapping("/{memberId}/status")
    public ResponseEntity<ApiResponse<Void>> updateMemberStatus(
            @PathVariable Long memberId,
            @RequestBody @Valid AdminMemberStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();

        adminMemberService.updateMemberStatus(memberId, request, adminEmail);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회원 role 변경
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @PathVariable Long memberId,
            @RequestBody @Valid AdminMemberRoleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String adminEmail = userDetails.getUsername();

        adminMemberService.updateMemberRole(memberId, request, adminEmail);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
