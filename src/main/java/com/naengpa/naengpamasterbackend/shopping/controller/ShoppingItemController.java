package com.naengpa.naengpamasterbackend.shopping.controller;

import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemCheckRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemCreateRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemMoveToFridgeRequest;
import com.naengpa.naengpamasterbackend.shopping.dto.request.ShoppingItemUpdateRequest;
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

@Tag(name = "장보기", description = "장보기 항목 CRUD 및 냉장고 반영 API")
@RestController
@RequestMapping("/api/v1/shopping-items")
public class ShoppingItemController {

    private final ShoppingItemService shoppingItemService;

    public ShoppingItemController(ShoppingItemService shoppingItemService) {
        this.shoppingItemService = shoppingItemService;
    }

    //장보기 등록
    @Operation(summary = "장보기 항목 추가", description = "로그인한 사용자의 장보기 목록에 사전 재료를 추가합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ShoppingItemResponse>> createShoppingItem(
            @Parameter(hidden = true)
            Authentication authentication,
            @Valid @RequestBody ShoppingItemCreateRequest request
            ) {
        ShoppingItemResponse response = shoppingItemService.createShoppingItem(authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("장보기 재료가 등록되었습니다.", response));

    }

    //장보기 조회
    @Operation(summary = "장보기 목록 조회", description = "로그인한 사용자의 장보기 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShoppingItemListResponse>>> findShoppingItems(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(shoppingItemService.findShoppingItems(authentication.getName()))
        );
    }

    //장보기 항목 삭제
    @Operation(summary = "장보기 항목 삭제", description = "로그인한 사용자의 장보기 항목을 삭제 처리합니다.")
    @DeleteMapping("/{shoppingItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteShoppingItem(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "장보기 항목 ID", example = "1", required = true)
            @Valid @PathVariable Long shoppingItemId
    ) {
        shoppingItemService.deleteShoppingItem(authentication.getName(), shoppingItemId);
        return ResponseEntity.ok(
                ApiResponse.success("장보기 항목이 삭제되었습니다.", null)
        );
    }

    //장보기 구매 여부
    @Operation(summary = "장보기 항목 체크/체크 해제", description = "장보기 항목의 구매 완료 여부를 변경합니다.")
    @PatchMapping("/{shoppingItemId}/check")
    public ResponseEntity<ApiResponse<ShoppingItemResponse>> updateShoppingItemPurchased(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "장보기 항목 ID", example = "1", required = true)
            @PathVariable Long shoppingItemId,
            @Valid @RequestBody ShoppingItemCheckRequest request
    ) {
        ShoppingItemResponse response = shoppingItemService.updateShoppingItemPurchased(
                authentication.getName(),
                shoppingItemId,
                request
        );

        return ResponseEntity.ok(ApiResponse.success("장보기 항목 체크 상태가 변경되었습니다.", response));
    }

    //장보기 항목 수정
    @Operation(summary = "장보기 항목 수정", description = "장보기 항목의 수량을 수정합니다.")
    @PatchMapping("/{shoppingItemId}")
    public ResponseEntity<ApiResponse<ShoppingItemResponse>> updateShoppingItem(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "장보기 항목 ID", example = "1", required = true)
            @PathVariable Long shoppingItemId,
            @Valid @RequestBody ShoppingItemUpdateRequest request
    ) {
        ShoppingItemResponse response = shoppingItemService.updateShoppingItem(
                authentication.getName(),
                shoppingItemId,
                request
        );

        return ResponseEntity.ok(ApiResponse.success("장보기 항목이 수정되었습니다.", response));
    }

    //장보기 항목 냉장고 추가
    @Operation(summary = "장보기 항목 냉장고 반영", description = "장보기 항목을 냉장고 재료로 등록합니다.")
    @PostMapping("/{shoppingItemId}/fridge")
    public ResponseEntity<ApiResponse<FridgeItemResponse>> moveShoppingItemToFridge(
            @Parameter(hidden = true)
            Authentication authentication,
            @Parameter(description = "장보기 항목 ID", example = "1", required = true)
            @PathVariable Long shoppingItemId,
            @Valid @RequestBody(required = false) ShoppingItemMoveToFridgeRequest request
    ) {
        FridgeItemResponse response = shoppingItemService.moveShoppingItemToFridge(
                authentication.getName(),
                shoppingItemId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("장보기 항목이 냉장고에 반영되었습니다.", response));
    }
}
