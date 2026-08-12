package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminProductCreateRequest;
import com.naengpa.naengpamasterbackend.admin.dto.request.AdminProductUpdateRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminProductResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminProductPageResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminProductService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 사전 재료", description = "관리자 사전 재료 관리 API")
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    //사전 재료 전체 조회 (활성 + 비활성)
    @Operation(summary = "사전 재료 전체 목록 조회", description = "관리자가 활성/비활성 사전 재료 전체 목록을 조회합니다.")
    @GetMapping()
    public ResponseEntity<ApiResponse<AdminProductPageResponse>> findAllProducts(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "productId", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminProductService.findAllProducts(search, pageable)));
    }

    //비활성 사전 재료 조회
    @Operation(summary = "비활성 사전 재료 목록 조회", description = "관리자가 비활성화된 사전 재료 목록을 조회합니다.")
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<AdminProductResponse>>> findInactiveProducts(){
        return ResponseEntity.ok(ApiResponse.success((adminProductService.findInactiveProducts())));
    }

    //어드민 사전 재료 추가
    @Operation(summary = "사전 재료 추가", description = "관리자가 사전 재료를 추가합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<AdminProductResponse>> createProduct(
            @Valid  @RequestBody AdminProductCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "사전 재료가 등록되었습니다.", adminProductService.createProduct(request)
                ));
    }

    //어드민 재료 수정
    @Operation(summary = "사전 재료 수정", description = "관리자가 사전 재료명, 카테고리, 기본 유통기한을 수정합니다.")
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<AdminProductResponse>> updateProduct(
            @Parameter(description = "사전 재료 ID", example = "1", required = true)
            @PathVariable Long productId,
            @Valid  @RequestBody AdminProductUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                        "사전 재료가 수정되었습니다.", adminProductService.updateProduct(productId, request)
                ));
    }

    // 어드민 사전 재료 비활성화
    @Operation(summary = "사전 재료 비활성화", description = "관리자가 사전 재료를 비활성화합니다.")
    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<ApiResponse<AdminProductResponse>> deactivateProduct(
            @Parameter(description = "사전 재료 ID", example = "1", required = true)
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "사전 재료가 비활성화되었습니다.",
                adminProductService.deactivateProduct(productId)
        ));
    }

    // 어드민 사전 재료 재활성화
    @Operation(summary = "사전 재료 재활성화", description = "관리자가 비활성화된 사전 재료를 다시 활성화합니다.")
    @PatchMapping("/{productId}/activate")
    public ResponseEntity<ApiResponse<AdminProductResponse>> activateProduct(
            @Parameter(description = "사전 재료 ID", example = "1", required = true)
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "사전 재료가 재활성화되었습니다.",
                adminProductService.activateProduct(productId)
        ));
    }

}
