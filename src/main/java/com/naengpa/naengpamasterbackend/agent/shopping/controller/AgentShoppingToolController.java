package com.naengpa.naengpamasterbackend.agent.shopping.controller;

import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemListResponse;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeItemService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemCreateRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.response.ShoppingItemListResponse;
import com.naengpa.naengpamasterbackend.shopping.dto.response.ShoppingItemResponse;
import com.naengpa.naengpamasterbackend.shopping.service.ShoppingItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agent/tools")
@Tag(name = "AI 냉장고 재료", description="AI 냉장고 재료 검색 API")
public class AgentShoppingToolController {

    private final FridgeItemService fridgeItemService;
    private final ShoppingItemService shoppingItemService;

    public AgentShoppingToolController(FridgeItemService fridgeItemService, ShoppingItemService shoppingItemService) {
        this.fridgeItemService = fridgeItemService;
        this.shoppingItemService = shoppingItemService;
    }


    // AI 냉장고 재료 조회 Tool
    @Operation(
            summary = "AI 냉장고 재료",
            description = "AI 추천 생성을 위해 로그인 사용자의 냉장고 재료 목록을 조회한다."
    )
    @GetMapping("/fridge-items")
    public ResponseEntity<ApiResponse<List<FridgeItemListResponse>>> findFridgeItems(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(fridgeItemService.findFridgeItem(authentication.getName()))
        );
    }

    // AI 장보기 목록 조회 Tool
    @Operation(
            summary = "AI 장보기 목록 조회",
            description = "AI 추천 생성을 위해 로그인 사용자의 장보기 목록을 조회한다."
    )
    @GetMapping("/shopping-items")
    public ResponseEntity<ApiResponse<List<ShoppingItemListResponse>>> findShoppingItems(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(shoppingItemService.findShoppingItems(authentication.getName()))
        );
    }

    // AI 장보기 추천 생성

    // 승인 추천 항목 장보기 추가
    @Operation(
            summary = "AI 승인 장보기 항목 추가",
            description = "AI 추천 결과 중 사용자가 승인한 항목을 장보기 목록에 추가합니다."
    )
    @PostMapping("/shopping-items")
    public ResponseEntity<ApiResponse<ShoppingItemResponse>> createShoppingItemByAgent(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody ShoppingItemCreateRequest request
    ) {
        ShoppingItemResponse response = shoppingItemService.createShoppingItem(
                authentication.getName(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("AI 추천 장보기 항목이 등록되었습니다.", response));
    }
}