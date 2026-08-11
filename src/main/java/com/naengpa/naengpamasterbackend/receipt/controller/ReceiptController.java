package com.naengpa.naengpamasterbackend.receipt.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.receipt.dto.response.ReceiptImageUploadResponse;
import com.naengpa.naengpamasterbackend.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/receipts")
@Tag(name = "영수증 Agent", description = "영수증 이미지 업로드 및 OCR 분석 API")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @Operation(
            summary = "영수증 이미지 업로드",
            description = "로그인 사용자의 영수증 이미지를 S3에 임시 저장하고 OCR 분석 대기 상태를 생성합니다."
    )
    // 이미지 파일은 JSON이 아니라 multipart/form-data 형식으로 전송되므로 consumes를 명시
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReceiptImageUploadResponse>> uploadReceiptImage(
            @Parameter(hidden = true) Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        ReceiptImageUploadResponse response = receiptService.uploadReceiptImage(
                authentication.getName(),
                file
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("영수증 이미지가 업로드되었습니다.", response));
    }
}
