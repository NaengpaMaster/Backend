package com.naengpa.naengpamasterbackend.receipt.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptAnalysisItemUpdateRequest;
import com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptFridgeRegisterRequest;
import com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptOcrSaveRequest;
import com.naengpa.naengpamasterbackend.receipt.dto.response.ReceiptAnalysisItemResponse;
import com.naengpa.naengpamasterbackend.receipt.dto.response.ReceiptImageUploadResponse;
import com.naengpa.naengpamasterbackend.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @Operation(
            summary = "영수증 OCR 후보 목록 조회",
            description = "로그인 사용자의 영수증 OCR 후보 항목 목록을 조회합니다."
    )
    // OCR 분석 후 저장된 후보들을 화면에서 확인하기 위한 조회 API
    @GetMapping("/{receiptAnalysisId}/items")
    public ResponseEntity<ApiResponse<List<ReceiptAnalysisItemResponse>>> getReceiptAnalysisItems(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long receiptAnalysisId
    ) {
        List<ReceiptAnalysisItemResponse> response = receiptService.getReceiptAnalysisItems(
                authentication.getName(),
                receiptAnalysisId
        );

        return ResponseEntity.ok(
                ApiResponse.success("영수증 OCR 후보 목록 조회에 성공했습니다.", response)
        );
    }

    @Operation(
            summary = "영수증 OCR 결과 저장 및 후보 자동 매칭",
            description = "Agent OCR 결과를 저장하고 사전 재료와 매칭된 후보 항목을 생성합니다."
    )
    // TODO: 실제 Agent 연동 후에는 백엔드 내부 호출 흐름으로 변경 예정
    @PostMapping("/{receiptAnalysisId}/ocr-results")
    public ResponseEntity<ApiResponse<List<ReceiptAnalysisItemResponse>>> saveOcrResult(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long receiptAnalysisId,
            @RequestBody ReceiptOcrSaveRequest request
    ) {
        List<ReceiptAnalysisItemResponse> response = receiptService.saveOcrResult(
                authentication.getName(),
                receiptAnalysisId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("영수증 OCR 결과가 저장되었습니다.", response)
        );
    }

    @Operation(summary = "영수증 OCR 후보 수정", description = "냉장고 등록 전 PENDING 상태 후보의 재료와 수량을 수정합니다.")
    @PatchMapping("/items/{receiptItemId}")
    public ResponseEntity<ApiResponse<ReceiptAnalysisItemResponse>> updateReceiptAnalysisItem(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long receiptItemId,
            @Valid @RequestBody ReceiptAnalysisItemUpdateRequest request
    ) {
        ReceiptAnalysisItemResponse response = receiptService.updateReceiptAnalysisItem(
                authentication.getName(),
                receiptItemId,
                request
        );

        return ResponseEntity.ok(ApiResponse.success("영수증 OCR 후보가 수정되었습니다.", response));
    }

    @Operation(summary = "영수증 OCR 후보 제외", description = "냉장고에 등록하지 않을 후보를 REJECTED 상태로 변경합니다.")
    @PatchMapping("/items/{receiptItemId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectReceiptAnalysisItem(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long receiptItemId
    ) {
        receiptService.rejectReceiptAnalysisItem(authentication.getName(), receiptItemId);
        return ResponseEntity.ok(ApiResponse.success("영수증 OCR 후보가 제외되었습니다.", null));
    }

    @Operation(summary = "영수증 OCR 후보 냉장고 등록", description = "선택한 PENDING 후보 또는 전체 PENDING 후보를 냉장고에 등록합니다.")
    @PostMapping("/{receiptAnalysisId}/fridge-items")
    public ResponseEntity<ApiResponse<List<FridgeItemResponse>>> registerReceiptItemsToFridge(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long receiptAnalysisId,
            @RequestBody(required = false) ReceiptFridgeRegisterRequest request
    ) {
        List<FridgeItemResponse> response = receiptService.registerReceiptItemsToFridge(
                authentication.getName(),
                receiptAnalysisId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("영수증 OCR 후보가 냉장고에 등록되었습니다.", response));
    }
}
