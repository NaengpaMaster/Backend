package com.naengpa.naengpamasterbackend.fridge.controller;

import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemCreateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemUpdateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemUsePartialRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemListResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeItemService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "냉장고 재료", description = "냉장고 재료 CRUD 및 유통기한 조회 API")
@RestController
@RequestMapping("/api/v1/fridge-items")
public class FridgeItemController {

    private final FridgeItemService fridgeItemService;

    public FridgeItemController(FridgeItemService fridgeItemService) {
        this.fridgeItemService = fridgeItemService;
    }

    //재료 등록
    @Operation(summary = "냉장고 재료 등록", description = "로그인한 사용자의 냉장고에 사전 재료를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<FridgeItemResponse>> createFridgeItem(
            @Parameter(hidden = true)
            Authentication authentication,
            @Valid @RequestBody FridgeItemCreateRequest request) {
        FridgeItemResponse response = fridgeItemService.createFridgeItem(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("냉장고 재료가 등록되었습니다.", response));
    }

    //냉장고 재료 조회
    @Operation(summary = "냉장고 재료 목록 조회", description = "로그인한 사용자의 냉장고 재료 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FridgeItemListResponse>>> findFridgeItems(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(fridgeItemService.findFridgeItem(authentication.getName())));
    }

    //냉장고 카테고리별 조회
    @Operation(summary = "냉장고 재료 카테고리별 조회", description = "로그인한 사용자의 냉장고 재료를 카테고리 기준으로 조회합니다.")
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<List<FridgeItemListResponse>>> findFridgeItemsByCategory(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "카테고리 ID", example = "1", required = true)
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(fridgeItemService.findFridgeItemsByCategory(authentication.getName(), categoryId))
        );
    }

    //냉장고 재료 수정
    @Operation(summary = "냉장고 재료 수정", description = "로그인한 사용자의 냉장고 재료 정보를 수정합니다.")
    @PatchMapping("/{fridgeItemId}")
    public ResponseEntity<ApiResponse<FridgeItemResponse>> updateFridgeItem(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "냉장고 재료 ID", example = "1", required = true)
            @PathVariable Long fridgeItemId,
            @Valid @RequestBody FridgeItemUpdateRequest request
    ) {
        FridgeItemResponse response = fridgeItemService.updateFridgeItem(authentication.getName(), fridgeItemId, request);
        return ResponseEntity.ok(ApiResponse.success("냉장고 재료가 수정되었습니다.", response));
    }

    //냉장고 재료 삭제
    @Operation(summary = "냉장고 재료 삭제", description = "로그인한 사용자의 냉장고 재료를 삭제 처리합니다.")
    @DeleteMapping("/{fridgeItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteFridgeItem(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "냉장고 재료 ID", example = "1", required = true)
            @PathVariable Long fridgeItemId
    ) {
        fridgeItemService.deleteFridgeItem(authentication.getName(), fridgeItemId);
        return ResponseEntity.ok(ApiResponse.success("냉장고 재료가 삭제되었습니다.", null));
    }

    //냉장고 재료 전부 사용
    @Operation(summary = "냉장고 재료 전부 사용 처리", description = "냉장고 재료를 전부 사용 처리하고 목록에서 제외합니다.")
    @PatchMapping("/{fridgeItemId}/use-all")
    public ResponseEntity<ApiResponse<Void>> useAllFridgeItem(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "냉장고 재료 ID", example = "1", required = true)
            @PathVariable Long fridgeItemId
    ) {
        fridgeItemService.useAllFridgeItem(authentication.getName(), fridgeItemId);
        return ResponseEntity.ok(ApiResponse.success("냉장고 재료를 전부 사용 처리했습니다.", null));
    }

    //냉장고 재료 일부 사용
    @Operation(summary = "냉장고 재료 일부 사용 처리", description = "냉장고 재료 일부 사용 후 남은 수량으로 수정합니다.")
    @PatchMapping("/{fridgeItemId}/use-partial")
    public ResponseEntity<ApiResponse<FridgeItemResponse>> usePartialFridgeItem(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "냉장고 재료 ID", example = "1", required = true)
            @PathVariable Long fridgeItemId,
            @Valid @RequestBody FridgeItemUsePartialRequest request
    ) {
        FridgeItemResponse response = fridgeItemService.usePartialFridgeItem(
                authentication.getName(),
                fridgeItemId,
                request
        );
        return ResponseEntity.ok(ApiResponse.success("냉장고 재료를 일부 사용 처리했습니다.", response));
    }

    //유통기한 임박 재료 조회
    @Operation(summary = "유통기한 임박 재료 조회", description = "로그인한 사용자의 냉장고 재료 중 유통기한이 임박한 재료를 조회합니다.")
    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<FridgeItemListResponse>>> findExpiringSoonFridgeItems(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(fridgeItemService.findExpiringSoonFridgeItems(authentication.getName())));
    }

    //만료 재료 조회
    @Operation(summary = "만료 재료 조회", description = "로그인한 사용자의 냉장고 재료 중 유통기한이 지난 재료를 조회합니다.")
    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<FridgeItemListResponse>>> findExpiredFridgeItems(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(fridgeItemService.findExpiredFridgeItems(authentication.getName())));
    }
}
