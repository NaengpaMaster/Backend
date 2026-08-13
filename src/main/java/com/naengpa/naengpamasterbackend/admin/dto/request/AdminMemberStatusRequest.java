package com.naengpa.naengpamasterbackend.admin.dto.request;

import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminMemberStatusRequest(
        @Schema(description = "변경할 회원 상태", example = "INACTIVE")
        @NotNull(message = "변경할 회원상태는 필수입니다.")
        MemberStatus status
) {}
