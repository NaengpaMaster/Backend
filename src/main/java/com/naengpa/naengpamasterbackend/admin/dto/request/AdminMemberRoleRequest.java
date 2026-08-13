package com.naengpa.naengpamasterbackend.admin.dto.request;

import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminMemberRoleRequest(
        @Schema(description = "변경할 회원 역할", example = "ADMIN")
        @NotNull(message = "role은 필수입니다.")
        MemberRole role) {
}
