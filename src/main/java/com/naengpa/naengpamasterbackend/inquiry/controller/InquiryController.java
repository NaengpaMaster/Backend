package com.naengpa.naengpamasterbackend.inquiry.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.inquiry.dto.request.InquiryRequest;
import com.naengpa.naengpamasterbackend.inquiry.dto.response.InquiryDetailResponse;
import com.naengpa.naengpamasterbackend.inquiry.dto.response.InquiryResponse;
import com.naengpa.naengpamasterbackend.inquiry.service.InquiryService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 문의", description = "사용자 본인의 문의 관리 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 목록 조회
    @Operation(summary = "내 문의 목록 조회", description = "로그인한 사용자가 본인의 문의 목록을 페이지 단위로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getInquiries(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(sort = "createdAt") Pageable pageable
    ) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(ApiResponse.success(inquiryService.getInquiries(email, pageable)));
    }

    // 상세 조회
    @Operation(summary = "내 문의 상세 조회", description = "로그인한 사용자가 본인의 문의와 답변을 조회합니다. 다른 사용자의 문의는 조회할 수 없습니다.")
    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<InquiryDetailResponse>> getInquiryDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "문의 ID", example = "1") @PathVariable Long inquiryId
    ) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(ApiResponse.success(inquiryService.getInquiryDetail(inquiryId, email)));
    }

    // 등록
    @Operation(summary = "문의 등록", description = "로그인한 사용자가 새 문의를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createInquiry(
            @RequestBody @Valid InquiryRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        inquiryService.createInquiry(request, email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 수정
    @Operation(summary = "내 문의 수정", description = "로그인한 사용자가 본인의 문의를 수정합니다. 다른 사용자의 문의는 수정할 수 없습니다.")
    @PatchMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> updateInquiry(
            @Parameter(description = "문의 ID", example = "1") @PathVariable Long inquiryId,
            @RequestBody @Valid InquiryRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        inquiryService.updateInquiry(inquiryId, request, email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 삭제
    @Operation(summary = "내 문의 삭제", description = "로그인한 사용자가 본인의 문의를 삭제 처리합니다. 다른 사용자의 문의는 삭제할 수 없습니다.")
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(
            @Parameter(description = "문의 ID", example = "1") @PathVariable Long inquiryId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        inquiryService.deleteInquiry(inquiryId, email);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
