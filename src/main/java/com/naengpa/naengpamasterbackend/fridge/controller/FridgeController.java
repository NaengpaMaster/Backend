package com.naengpa.naengpamasterbackend.fridge.controller;

import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeMemberInviteRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeAccessResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeInviteResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeMemberResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeResponse;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fridges")
@RequiredArgsConstructor
public class FridgeController {

    private final FridgeService fridgeService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<FridgeResponse>> getMyDefaultFridge(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getMyDefaultFridge(authentication.getName())));
    }

    @GetMapping("/accessible")
    public ResponseEntity<ApiResponse<List<FridgeAccessResponse>>> getAccessibleFridges(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getAccessibleFridges(authentication.getName())));
    }

    @GetMapping("/me/members")
    public ResponseEntity<ApiResponse<List<FridgeMemberResponse>>> getMyFridgeMembers(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getMyFridgeMembers(authentication.getName())));
    }

    @PostMapping("/me/members")
    public ResponseEntity<ApiResponse<FridgeInviteResponse>> inviteMember(
            Authentication authentication,
            @Valid @RequestBody FridgeMemberInviteRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "가족 냉장고 공유를 신청했습니다.",
                fridgeService.inviteMember(authentication.getName(), request.email())
        ));
    }

    @GetMapping("/me/invites")
    public ResponseEntity<ApiResponse<List<FridgeInviteResponse>>> getMyFridgePendingInvites(
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getMyFridgePendingInvites(authentication.getName())));
    }

    @GetMapping("/invites/received")
    public ResponseEntity<ApiResponse<List<FridgeInviteResponse>>> getReceivedInvites(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getPendingInvites(authentication.getName())));
    }

    @PostMapping("/invites/{inviteId}/accept")
    public ResponseEntity<ApiResponse<FridgeMemberResponse>> acceptInvite(
            Authentication authentication,
            @PathVariable Long inviteId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "가족 냉장고 공유 신청을 수락했습니다.",
                fridgeService.acceptInvite(authentication.getName(), inviteId)
        ));
    }

    @PostMapping("/invites/{inviteId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectInvite(
            Authentication authentication,
            @PathVariable Long inviteId
    ) {
        fridgeService.rejectInvite(authentication.getName(), inviteId);
        return ResponseEntity.ok(ApiResponse.success("가족 냉장고 공유 신청을 거절했습니다.", null));
    }

    @DeleteMapping("/me/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            Authentication authentication,
            @PathVariable Long memberId
    ) {
        fridgeService.removeMember(authentication.getName(), memberId);
        return ResponseEntity.ok(ApiResponse.success("가족 구성원이 제거되었습니다.", null));
    }
}
