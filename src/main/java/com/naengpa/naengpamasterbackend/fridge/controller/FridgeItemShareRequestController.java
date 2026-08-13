package com.naengpa.naengpamasterbackend.fridge.controller;

import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemShareRequestAcceptRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemShareRequestResponse;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeItemService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "식재료 공유 요청", description = "식재료 요청 수락/거절 API")
@RestController
@RequestMapping("/api/v1/fridge-item-share-requests")
public class FridgeItemShareRequestController {

    private final FridgeItemService fridgeItemService;

    public FridgeItemShareRequestController(FridgeItemService fridgeItemService) {
        this.fridgeItemService = fridgeItemService;
    }

    @Operation(summary = "식재료 요청 수락", description = "받은 식재료 요청을 수락하고 요청자 냉장고로 재료를 전달합니다.")
    @PatchMapping("/{shareRequestId}/accept")
    public ResponseEntity<ApiResponse<FridgeItemResponse>> acceptShareRequest(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long shareRequestId,
            @Valid @RequestBody FridgeItemShareRequestAcceptRequest request
    ) {
        FridgeItemResponse response = fridgeItemService.acceptShareRequest(authentication.getName(), shareRequestId, request);
        return ResponseEntity.ok(ApiResponse.success("식재료 요청을 수락했습니다.", response));
    }

    @Operation(summary = "식재료 요청 거절", description = "받은 식재료 요청을 거절합니다.")
    @PatchMapping("/{shareRequestId}/reject")
    public ResponseEntity<ApiResponse<FridgeItemShareRequestResponse>> rejectShareRequest(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long shareRequestId
    ) {
        FridgeItemShareRequestResponse response = fridgeItemService.rejectShareRequest(authentication.getName(), shareRequestId);
        return ResponseEntity.ok(ApiResponse.success("식재료 요청을 거절했습니다.", response));
    }
}
