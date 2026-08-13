package com.naengpa.naengpamasterbackend.inquiry.knowledge.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.request.InquiryKnowledgeDocumentRequest;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response.InquiryKnowledgeDocumentResponse;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.service.InquiryKnowledgeService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 문의 Q&A 정책 문서", description = "문의 Q&A 챗봇 정책 문서 등록 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/inquiry-knowledge/documents")
@RequiredArgsConstructor
public class AdminInquiryKnowledgeController {

    private final InquiryKnowledgeService inquiryKnowledgeService;

    @Operation(summary = "문의 Q&A 정책 문서 등록", description = "관리자가 챗봇 답변에 사용할 정책 문서를 등록하고 검색용 청크를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<InquiryKnowledgeDocumentResponse>> saveDocument(
            @Valid @RequestBody InquiryKnowledgeDocumentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inquiryKnowledgeService.saveDocument(request)));
    }
}
