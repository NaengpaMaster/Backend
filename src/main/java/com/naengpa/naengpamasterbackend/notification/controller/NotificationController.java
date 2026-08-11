package com.naengpa.naengpamasterbackend.notification.controller;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.notification.dto.response.NotificationResponse;
import com.naengpa.naengpamasterbackend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "알림", description = "소비기한 임박/만료, 문의 답변, 식재료 요청 등 사용자 알림 조회 및 확인 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "읽지 않은 알림 조회", description = "로그인한 사용자의 읽지 않은 알림 목록을 조회합니다. 홈 팝업과 식재료 요청/나눔 알림에 사용됩니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadNotifications(authentication.getName())));
    }

    @Operation(summary = "알림 단건 확인", description = "특정 알림을 읽음 상태로 변경합니다. 본인 알림만 확인할 수 있습니다.")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "읽음 처리할 알림 ID") @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(authentication.getName(), notificationId);
        return ResponseEntity.ok(ApiResponse.success("알림을 확인했습니다.", null));
    }

    @Operation(summary = "모든 알림 확인", description = "로그인한 사용자의 읽지 않은 알림을 모두 읽음 상태로 변경합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @Parameter(hidden = true) Authentication authentication
    ) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("모든 알림을 확인했습니다.", null));
    }
}
