package com.naengpa.naengpamasterbackend.inquiry.knowledge.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.request.InquiryKnowledgeDocumentRequest;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response.InquiryKnowledgeDocumentResponse;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.service.InquiryKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/inquiry-knowledge/documents")
@RequiredArgsConstructor
public class AdminInquiryKnowledgeController {

    private final InquiryKnowledgeService inquiryKnowledgeService;

    @PostMapping
    public ResponseEntity<ApiResponse<InquiryKnowledgeDocumentResponse>> saveDocument(
            @Valid @RequestBody InquiryKnowledgeDocumentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inquiryKnowledgeService.saveDocument(request)));
    }
}

