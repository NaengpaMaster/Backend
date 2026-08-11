package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.entity.Inquiry;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminInquiryResponse(
        @Schema(description = "문의 ID", example = "1") Long inquiryId,
        @Schema(description = "작성 회원 ID", example = "10") Long memberId,
        @Schema(description = "작성 회원 닉네임", example = "냉파초보") String nickname,
        @Schema(description = "문의 제목", example = "재료 등록 방법 문의") String title,
        @Schema(description = "답변 완료 여부", example = "false") Boolean isAnswered,
        @Schema(description = "문의 등록 시각", example = "2026-08-11T09:00:00") LocalDateTime createdAt
) {

    public static AdminInquiryResponse from(Inquiry inquiry, String nickname) {
        return new AdminInquiryResponse(
                inquiry.getId(),
                inquiry.getMemberId(),
                nickname,
                inquiry.getTitle(),
                inquiry.getIsAnswered(),
                inquiry.getCreatedAt()
        );
    }
}
