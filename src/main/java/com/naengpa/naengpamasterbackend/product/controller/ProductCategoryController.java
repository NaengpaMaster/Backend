package com.naengpa.naengpamasterbackend.product.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.product.dto.response.ProductCategoryResponse;
import com.naengpa.naengpamasterbackend.product.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "카테고리", description = "사전 재료 카테고리 API")
@RestController
@RequestMapping("/api/v1/categories")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @Operation(summary = "카테고리 목록 조회", description = "냉장고와 장보기에서 사용할 사전 재료 카테고리 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> findCategories() {
        return ResponseEntity.ok(ApiResponse.success(productCategoryService.findCategories()));
    }


}
