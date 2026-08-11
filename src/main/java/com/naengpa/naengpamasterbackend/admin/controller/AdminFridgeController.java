package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminFridgeResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminFridgeService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/fridges")
@RequiredArgsConstructor
@Tag(name = "관리자 가족 공유 냉장고", description = "관리자 가족 공유 냉장고 조회 및 최소 운영 조치 API")
public class AdminFridgeController {

    private final AdminFridgeService adminFridgeService;

    @Operation(summary = "가족 공유 냉장고 목록 조회", description = "관리자가 가족 공유 냉장고 목록과 구독/구성원 상태를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminFridgeResponse>>> getFridges() {
        return ResponseEntity.ok(ApiResponse.success(adminFridgeService.getFridges()));
    }

    @Operation(summary = "가족 공유 냉장고 상세 조회", description = "관리자가 특정 가족 공유 냉장고의 구성원과 초대 상태를 조회합니다.")
    @GetMapping("/{fridgeId}")
    public ResponseEntity<ApiResponse<AdminFridgeResponse>> getFridge(
            @Parameter(description = "조회할 냉장고 ID") @PathVariable Long fridgeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminFridgeService.getFridge(fridgeId)));
    }


    @Operation(summary = "가족 공유 초대 취소", description = "관리자가 특정 냉장고의 대기 중인 가족 공유 초대를 취소합니다.")
    @DeleteMapping("/{fridgeId}/invites/{inviteId}")
    public ResponseEntity<ApiResponse<Void>> cancelInvite(
            @Parameter(description = "냉장고 ID") @PathVariable Long fridgeId,
            @Parameter(description = "취소할 초대 ID") @PathVariable Long inviteId
    ) {
        adminFridgeService.cancelInvite(fridgeId, inviteId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가족 구성원 내보내기", description = "관리자가 구독 냉장고의 가족 구성원을 최소 조치로 내보냅니다. 냉장고 owner는 내보낼 수 없습니다.")
    @DeleteMapping("/{fridgeId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @Parameter(description = "냉장고 ID") @PathVariable Long fridgeId,
            @Parameter(description = "내보낼 구성원 회원 ID") @PathVariable Long memberId
    ) {
        adminFridgeService.removeMember(fridgeId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
