package com.naengpa.naengpamasterbackend.fridge.controller;

import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeMemberInviteRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeAccessResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeInviteResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeMemberResponse;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeResponse;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "가족 공유 냉장고", description = "개인 냉장고 조회, 가족 공유 초대/수락/거절, 접근 가능한 냉장고 조회 API")
public class FridgeController {

    private final FridgeService fridgeService;

    @Operation(summary = "내 기본 냉장고 조회", description = "로그인한 사용자의 기본 개인 냉장고 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<FridgeResponse>> getMyDefaultFridge(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getMyDefaultFridge(authentication.getName())));
    }

    @Operation(summary = "접근 가능한 냉장고 목록 조회", description = "내 냉장고와 가족에게 공유받은 냉장고 목록을 조회합니다.")
    @GetMapping("/accessible")
    public ResponseEntity<ApiResponse<List<FridgeAccessResponse>>> getAccessibleFridges(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getAccessibleFridges(authentication.getName())));
    }

    @Operation(summary = "내 냉장고 가족 구성원 조회", description = "프리미엄 냉장고 owner가 본인 냉장고의 가족 구성원 목록을 조회합니다.")
    @GetMapping("/me/members")
    public ResponseEntity<ApiResponse<List<FridgeMemberResponse>>> getMyFridgeMembers(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getMyFridgeMembers(authentication.getName())));
    }

    @Operation(summary = "가족 구성원 초대", description = "프리미엄 냉장고 owner가 가입된 회원 이메일로 가족 공유 초대를 생성합니다. owner 포함 최대 4명까지 가능합니다.")
    @PostMapping("/me/members")
    public ResponseEntity<ApiResponse<FridgeInviteResponse>> inviteMember(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody FridgeMemberInviteRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "가족 냉장고 공유를 신청했습니다.",
                fridgeService.inviteMember(authentication.getName(), request.email())
        ));
    }

    @Operation(summary = "내 냉장고 보낸 초대 조회", description = "내 냉장고에 대해 아직 수락/거절되지 않은 가족 공유 초대 목록을 조회합니다.")
    @GetMapping("/me/invites")
    public ResponseEntity<ApiResponse<List<FridgeInviteResponse>>> getMyFridgePendingInvites(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getMyFridgePendingInvites(authentication.getName())));
    }

    @Operation(summary = "받은 가족 공유 초대 조회", description = "로그인한 사용자가 받은 가족 공유 초대 목록을 조회합니다.")
    @GetMapping("/invites/received")
    public ResponseEntity<ApiResponse<List<FridgeInviteResponse>>> getReceivedInvites(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(fridgeService.getPendingInvites(authentication.getName())));
    }

    @Operation(summary = "가족 공유 초대 수락", description = "받은 가족 공유 초대를 수락하고 해당 냉장고의 구성원으로 참여합니다.")
    @PostMapping("/invites/{inviteId}/accept")
    public ResponseEntity<ApiResponse<FridgeMemberResponse>> acceptInvite(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "수락할 가족 공유 초대 ID") @PathVariable Long inviteId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "가족 냉장고 공유 신청을 수락했습니다.",
                fridgeService.acceptInvite(authentication.getName(), inviteId)
        ));
    }

    @Operation(summary = "가족 공유 초대 거절", description = "받은 가족 공유 초대를 거절합니다.")
    @PostMapping("/invites/{inviteId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectInvite(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "거절할 가족 공유 초대 ID") @PathVariable Long inviteId
    ) {
        fridgeService.rejectInvite(authentication.getName(), inviteId);
        return ResponseEntity.ok(ApiResponse.success("가족 냉장고 공유 신청을 거절했습니다.", null));
    }

    @Operation(summary = "가족 구성원 내보내기", description = "프리미엄 냉장고 owner가 가족 구성원을 내보냅니다. owner 본인은 제거할 수 없습니다.")
    @DeleteMapping("/me/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "내보낼 가족 구성원 회원 ID") @PathVariable Long memberId
    ) {
        fridgeService.removeMember(authentication.getName(), memberId);
        return ResponseEntity.ok(ApiResponse.success("가족 구성원이 제거되었습니다.", null));
    }
}
