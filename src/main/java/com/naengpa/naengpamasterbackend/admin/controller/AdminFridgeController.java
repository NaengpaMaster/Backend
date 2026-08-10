package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminFridgeResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminFridgeService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
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
public class AdminFridgeController {

    private final AdminFridgeService adminFridgeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminFridgeResponse>>> getFridges() {
        return ResponseEntity.ok(ApiResponse.success(adminFridgeService.getFridges()));
    }

    @GetMapping("/{fridgeId}")
    public ResponseEntity<ApiResponse<AdminFridgeResponse>> getFridge(@PathVariable Long fridgeId) {
        return ResponseEntity.ok(ApiResponse.success(adminFridgeService.getFridge(fridgeId)));
    }


    @DeleteMapping("/{fridgeId}/invites/{inviteId}")
    public ResponseEntity<ApiResponse<Void>> cancelInvite(
            @PathVariable Long fridgeId,
            @PathVariable Long inviteId
    ) {
        adminFridgeService.cancelInvite(fridgeId, inviteId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{fridgeId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long fridgeId,
            @PathVariable Long memberId
    ) {
        adminFridgeService.removeMember(fridgeId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
