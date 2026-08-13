package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminAnswerRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryDetailResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminInquiryService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 문의", description = "관리자 문의 조회 및 답변 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiriesController {

    private final AdminInquiryService adminInquiryService;

    // 문의 목록 조회 API
    @Operation(summary = "문의 목록 조회", description = "답변 여부와 정렬 방향을 기준으로 문의를 페이지 단위로 조회합니다. 미답변 문의는 기본 오래된 순, 답변 완료 문의는 기본 최신 순입니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminInquiryResponse>>> getInquiries(
            @Parameter(description = "답변 완료 여부", example = "false") @RequestParam Boolean isAnswered,
            @Parameter(description = "정렬 방향(ASC 또는 DESC)", example = "ASC") @RequestParam(required = false) Sort.Direction sortDirection,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        adminInquiryService.getInquiries(isAnswered, sortDirection, pageable)));
    }

    // 문의 상세 조회 API
    @Operation(summary = "문의 상세 조회", description = "문의 내용과 등록된 답변을 조회합니다. 관리자 권한이 필요합니다.")
    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<AdminInquiryDetailResponse>> getInquiryDetail(
            @Parameter(description = "문의 ID", example = "1") @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminInquiryService.getInquiryDetail(inquiryId)));
    }

    // 문의 답변 등록 API
    @Operation(summary = "문의 답변 등록", description = "미답변 문의에 답변을 등록합니다. 활성 답변은 문의당 하나만 등록할 수 있습니다.")
    @PostMapping("/{inquiryId}/answers")
    public ResponseEntity<ApiResponse<Void>> createInquiryAnswer(
            @Parameter(description = "문의 ID", example = "1") @PathVariable Long inquiryId,
            @RequestBody @Valid AdminAnswerRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
            ) {

        String adminEmail = userDetails.getUsername();
        adminInquiryService.createInquiryAnswer(inquiryId, request, adminEmail);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 문의 답변 수정 API
    @Operation(summary = "문의 답변 수정", description = "해당 문의의 활성 답변 내용을 수정합니다. 관리자 권한이 필요합니다.")
    @PatchMapping("/{inquiryId}/answers/{answerId}")
    public ResponseEntity<ApiResponse<Void>> updateInquiryAnswer(
            @Parameter(description = "문의 ID", example = "1") @PathVariable Long inquiryId,
            @Parameter(description = "답변 ID", example = "1") @PathVariable Long answerId,
            @RequestBody @Valid AdminAnswerRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {

        String email = userDetails.getUsername();
        adminInquiryService.updateInquiryAnswer(inquiryId, answerId, request, email);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 문의 답변 삭제 API
    @Operation(summary = "문의 답변 삭제", description = "해당 문의의 활성 답변을 삭제 처리합니다. 관리자 권한이 필요합니다.")
    @DeleteMapping("/{inquiryId}/answers/{answerId}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiryAnswer(
            @Parameter(description = "문의 ID", example = "1") @PathVariable Long inquiryId,
            @Parameter(description = "답변 ID", example = "1") @PathVariable Long answerId
    ) {
        adminInquiryService.deleteInquiryAnswer(inquiryId, answerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 문의 삭제 API
    @Operation(summary = "문의 삭제", description = "문의를 삭제 처리합니다. 관리자 권한이 필요합니다.")
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(
            @Parameter(description = "문의 ID", example = "1") @PathVariable Long inquiryId
    ) {
        adminInquiryService.deleteInquiry(inquiryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
