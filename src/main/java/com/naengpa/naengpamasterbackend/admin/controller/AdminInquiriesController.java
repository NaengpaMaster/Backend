package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminAnswerRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryDetailResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminInquiryService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiriesController {

    private final AdminInquiryService adminInquiryService;

    // 문의 목록 조회 API
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminInquiryResponse>>> getInquiries(
            @RequestParam Boolean isAnswered,
            @RequestParam(required = false) Sort.Direction sortDirection,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        adminInquiryService.getInquiries(isAnswered, sortDirection, pageable)));
    }

    // 문의 상세 조회 API
    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<AdminInquiryDetailResponse>> getInquiryDetail(
            @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminInquiryService.getInquiryDetail(inquiryId)));
    }

    // 문의 답변 등록 API
    @PostMapping("/{inquiryId}/answers")
    public ResponseEntity<ApiResponse<Void>> createInquiryAnswer(
            @PathVariable Long inquiryId,
            @RequestBody @Valid AdminAnswerRequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ) {

        String adminEmail = userDetails.getUsername();
        adminInquiryService.createInquiryAnswer(inquiryId, request, adminEmail);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 문의 답변 수정 API
    @PatchMapping("/{inquiryId}/answers/{answerId}")
    public ResponseEntity<ApiResponse<Void>> updateInquiryAnswer(
            @PathVariable Long inquiryId,
            @PathVariable Long answerId,
            @RequestBody @Valid AdminAnswerRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String email = userDetails.getUsername();
        adminInquiryService.updateInquiryAnswer(inquiryId, answerId, request, email);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 문의 답변 삭제 API
    @DeleteMapping("/{inquiryId}/answers/{answerId}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiryAnswer(
            @PathVariable Long inquiryId,
            @PathVariable Long answerId
    ) {
        adminInquiryService.deleteInquiryAnswer(inquiryId, answerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 문의 삭제 API
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(
            @PathVariable Long inquiryId
    ) {
        adminInquiryService.deleteInquiry(inquiryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}