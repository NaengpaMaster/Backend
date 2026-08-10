package com.naengpa.naengpamasterbackend.global.auth.oauth2.dto;

import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OAuth2EmailCompletionRequest(
        @NotBlank(message = "OAuth2 가입 토큰은 필수입니다.")
        String signupToken,
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,
        @Pattern(regexp = "^[가-힣A-Za-z0-9 ]*$", message = "닉네임은 한글, 영문, 숫자, 공백만 사용할 수 있습니다.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname,
        HouseholdType householdType
) {
}
