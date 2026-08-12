package com.naengpa.naengpamasterbackend.fridge.photo.controller;

import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.request.FridgePhotoItemUpdateRequest;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.request.FridgePhotoItemsRegisterRequest;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.request.FridgePhotoOcrSaveRequest;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.response.FridgePhotoImageUploadResponse;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.response.FridgePhotoItemResponse;
import com.naengpa.naengpamasterbackend.fridge.photo.service.FridgePhotoService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fridge-photos")
@Tag(name = "냉장고 사진 Agent", description = "냉장고 사진 기반 재료 후보 분석 및 등록 API")
public class FridgePhotoController {

    private final FridgePhotoService fridgePhotoService;
    private final SubscriptionService subscriptionService;

    public FridgePhotoController(
            FridgePhotoService fridgePhotoService,
            SubscriptionService subscriptionService
    ) {
        this.fridgePhotoService = fridgePhotoService;
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "냉장고 사진 업로드", description = "구독 중인 사용자만 냉장고 사진을 업로드하고 분석 대기 상태를 생성할 수 있습니다.")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FridgePhotoImageUploadResponse>> uploadImage(
            @Parameter(hidden = true) Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        validatePremium(authentication);
        FridgePhotoImageUploadResponse response = fridgePhotoService.uploadFridgePhoto(authentication.getName(), file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("냉장고 사진이 업로드되었습니다.", response));
    }

    @Operation(summary = "냉장고 사진 후보 목록 조회", description = "로그인 사용자의 냉장고 사진 분석 후보 항목 목록을 조회합니다.")
    @GetMapping("/{fridgePhotoAnalysisId}/items")
    public ResponseEntity<ApiResponse<List<FridgePhotoItemResponse>>> getItems(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long fridgePhotoAnalysisId
    ) {
        List<FridgePhotoItemResponse> response = fridgePhotoService.getItems(authentication.getName(), fridgePhotoAnalysisId);
        return ResponseEntity.ok(ApiResponse.success("냉장고 사진 후보 목록 조회에 성공했습니다.", response));
    }

    @Operation(summary = "냉장고 사진 분석 결과 저장 및 후보 자동 매칭", description = "Agent 분석 결과를 저장하고 사전 재료와 매칭된 후보 항목을 생성합니다.")
    @PostMapping("/{fridgePhotoAnalysisId}/analysis-results")
    public ResponseEntity<ApiResponse<List<FridgePhotoItemResponse>>> saveAnalysisResult(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long fridgePhotoAnalysisId,
            @RequestBody FridgePhotoOcrSaveRequest request
    ) {
        validatePremium(authentication);
        List<FridgePhotoItemResponse> response = fridgePhotoService.saveAnalysisResult(
                authentication.getName(),
                fridgePhotoAnalysisId,
                request
        );
        return ResponseEntity.ok(ApiResponse.success("냉장고 사진 분석 결과가 저장되었습니다.", response));
    }

    @Operation(summary = "냉장고 사진 후보 수정", description = "냉장고 등록 전 PENDING 상태 후보의 재료와 수량을 수정합니다.")
    @PatchMapping("/items/{fridgePhotoItemId}")
    public ResponseEntity<ApiResponse<FridgePhotoItemResponse>> updateItem(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long fridgePhotoItemId,
            @Valid @RequestBody FridgePhotoItemUpdateRequest request
    ) {
        validatePremium(authentication);
        FridgePhotoItemResponse response = fridgePhotoService.updateItem(authentication.getName(), fridgePhotoItemId, request);
        return ResponseEntity.ok(ApiResponse.success("냉장고 사진 후보가 수정되었습니다.", response));
    }

    @Operation(summary = "냉장고 사진 후보 제외", description = "냉장고에 등록하지 않을 후보를 REJECTED 상태로 변경합니다.")
    @PatchMapping("/items/{fridgePhotoItemId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectItem(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long fridgePhotoItemId
    ) {
        validatePremium(authentication);
        fridgePhotoService.rejectItem(authentication.getName(), fridgePhotoItemId);
        return ResponseEntity.ok(ApiResponse.success("냉장고 사진 후보가 제외되었습니다.", null));
    }

    @Operation(summary = "냉장고 사진 후보 냉장고 등록", description = "선택한 PENDING 후보 또는 전체 PENDING 후보를 냉장고에 등록합니다.")
    @PostMapping("/{fridgePhotoAnalysisId}/fridge-items")
    public ResponseEntity<ApiResponse<List<FridgeItemResponse>>> registerItems(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long fridgePhotoAnalysisId,
            @RequestBody(required = false) FridgePhotoItemsRegisterRequest request
    ) {
        validatePremium(authentication);
        List<FridgeItemResponse> response = fridgePhotoService.registerItems(
                authentication.getName(),
                fridgePhotoAnalysisId,
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("냉장고 사진 후보가 냉장고에 등록되었습니다.", response));
    }

    private void validatePremium(Authentication authentication) {
        if (!subscriptionService.getMySubscription(authentication.getName()).premium()) {
            throw new AccessDeniedException("냉장고 사진 등록은 구독 후 사용할 수 있습니다.");
        }
    }
}
