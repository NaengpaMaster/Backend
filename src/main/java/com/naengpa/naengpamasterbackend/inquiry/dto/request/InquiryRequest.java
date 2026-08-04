package com.naengpa.naengpamasterbackend.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InquiryRequest(
        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 300, message = "title은 300자 이하여야 합니다.")
        String title,

        @NotBlank(message = "content는 필수입니다.")
        @Size(max = 1000, message = "content는 1000자 이하여야 합니다.")
        String content
) {}
