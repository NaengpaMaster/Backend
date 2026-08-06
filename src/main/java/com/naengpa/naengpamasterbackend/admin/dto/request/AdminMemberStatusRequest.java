package com.naengpa.naengpamasterbackend.admin.dto.request;

import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import jakarta.validation.constraints.NotNull;

public record AdminMemberStatusRequest(
        @NotNull(message = "변경할 회원상태는 필수입니다.")
        MemberStatus status
) {}
