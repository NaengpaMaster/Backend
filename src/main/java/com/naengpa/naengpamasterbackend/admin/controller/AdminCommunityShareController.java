package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminCommunitySharePostResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminCommunityShareSummaryResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminCommunityShareService;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePostStatus;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 재료 함께 나눔", description = "관리자 재료 함께 나눔 이용 현황 및 게시글 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/community-shares")
@RequiredArgsConstructor
public class AdminCommunityShareController {

    private final AdminCommunityShareService adminCommunityShareService;

    @Operation(summary = "나눔 운영 요약 조회", description = "재료 함께 나눔 게시글 수와 참여 수를 조회합니다. 관리자 권한이 필요합니다.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminCommunityShareSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(adminCommunityShareService.getSummary()));
    }

    @Operation(summary = "나눔 게시글 목록 조회", description = "상태별 재료 함께 나눔 게시글을 페이지 단위로 조회합니다. 관리자 권한이 필요합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminCommunitySharePostResponse>>> getPosts(
            @Parameter(description = "게시글 상태", example = "OPEN")
            @RequestParam(required = false) CommunitySharePostStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminCommunityShareService.getPosts(status, pageable)));
    }

    @Operation(summary = "나눔 게시글 관리자 취소", description = "관리자가 부적절한 나눔 게시글을 취소 처리합니다. 관리자 권한이 필요합니다.")
    @PatchMapping("/{communitySharePostId}/cancel")
    public ResponseEntity<ApiResponse<AdminCommunitySharePostResponse>> cancelPost(
            @Parameter(description = "나눔 게시글 ID") @PathVariable Long communitySharePostId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "나눔 게시글을 취소했습니다.",
                adminCommunityShareService.cancelPost(communitySharePostId)
        ));
    }
}
