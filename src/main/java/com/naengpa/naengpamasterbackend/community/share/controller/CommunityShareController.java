package com.naengpa.naengpamasterbackend.community.share.controller;

import com.naengpa.naengpamasterbackend.community.share.dto.request.CommunitySharePostCreateRequest;
import com.naengpa.naengpamasterbackend.community.share.dto.request.CommunitySharePostSearchRequest;
import com.naengpa.naengpamasterbackend.community.share.dto.response.CommunitySharePostResponse;
import com.naengpa.naengpamasterbackend.community.share.service.CommunityShareService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "주변 식재료 나눔", description = "위치 기반 식재료 공동 나눔 모집 API")
@RestController
@RequestMapping("/api/v1/community-shares")
public class CommunityShareController {

    private final CommunityShareService communityShareService;

    public CommunityShareController(CommunityShareService communityShareService) {
        this.communityShareService = communityShareService;
    }

    @Operation(summary = "주변 나눔 목록 조회", description = "현재 위치 기준 반경 내 모집 중인 식재료 나눔을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CommunitySharePostResponse>>> findOpenPosts(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @ModelAttribute CommunitySharePostSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityShareService.findOpenPosts(authentication.getName(), request)));
    }

    @Operation(summary = "내 나눔 목록 조회", description = "내가 작성한 식재료 나눔 글을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<CommunitySharePostResponse>>> findMyPosts(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "페이지 번호", example = "0")
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityShareService.findMyPosts(authentication.getName(), page)));
    }

    @Operation(summary = "내가 참여한 나눔 목록 조회", description = "로그인한 회원이 참여 중인 식재료 나눔 글을 조회합니다.")
    @GetMapping("/me/joined")
    public ResponseEntity<ApiResponse<PageResponse<CommunitySharePostResponse>>> findMyJoinedPosts(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "페이지 번호", example = "0")
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(ApiResponse.success(communityShareService.findMyJoinedPosts(authentication.getName(), page)));
    }

    @Operation(summary = "나눔 글 등록", description = "큰 식재료를 주변 사용자와 나눠 갖기 위한 모집 글을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CommunitySharePostResponse>> createPost(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody CommunitySharePostCreateRequest request
    ) {
        CommunitySharePostResponse response = communityShareService.createPost(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("나눔 글이 등록되었습니다.", response));
    }

    @Operation(summary = "나눔 선착순 참여", description = "모집 중인 나눔에 선착순으로 참여합니다.")
    @PostMapping("/{communitySharePostId}/join")
    public ResponseEntity<ApiResponse<CommunitySharePostResponse>> joinPost(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long communitySharePostId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "나눔에 참여했습니다.",
                communityShareService.joinPost(authentication.getName(), communitySharePostId)
        ));
    }

    @Operation(summary = "나눔 참여 취소", description = "내가 참여한 나눔을 취소합니다.")
    @PatchMapping("/{communitySharePostId}/cancel-join")
    public ResponseEntity<ApiResponse<CommunitySharePostResponse>> cancelJoin(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long communitySharePostId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "나눔 참여를 취소했습니다.",
                communityShareService.cancelJoin(authentication.getName(), communitySharePostId)
        ));
    }

    @Operation(summary = "나눔 마감", description = "내가 작성한 나눔 글을 마감합니다.")
    @PatchMapping("/{communitySharePostId}/close")
    public ResponseEntity<ApiResponse<CommunitySharePostResponse>> closePost(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long communitySharePostId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "나눔 글을 마감했습니다.",
                communityShareService.closePost(authentication.getName(), communitySharePostId)
        ));
    }

    @Operation(summary = "나눔 취소", description = "내가 작성한 나눔 글을 취소합니다.")
    @PatchMapping("/{communitySharePostId}/cancel")
    public ResponseEntity<ApiResponse<CommunitySharePostResponse>> cancelPost(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long communitySharePostId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "나눔 글을 취소했습니다.",
                communityShareService.cancelPost(authentication.getName(), communitySharePostId)
        ));
    }
}
