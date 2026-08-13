package com.naengpa.naengpamasterbackend.product.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.product.dto.response.ProductSearchResponse;
import com.naengpa.naengpamasterbackend.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "사전 재료", description = "사전 재료 검색 API")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //사전 재료 검색 API-200
    @Operation(
            summary = "사전 재료 검색",
            description = "사용자가 입력한 검색어로 활성 상태의 사전 재료를 부분 검색합니다."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductSearchResponse>>> searchProducts(
            @Parameter(description = "검색어", example = "두부", required = true)
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(keyword)));
    }
}
