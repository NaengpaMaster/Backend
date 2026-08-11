package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.entity.Inquiry;
import com.naengpa.naengpamasterbackend.inquiry.entity.InquiryAnswer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminInquiryDetailResponse(
        @Schema(description = "문의 ID", example = "1") Long inquiryId,
        @Schema(description = "작성 회원 ID", example = "10") Long memberId,
        @Schema(description = "문의 제목", example = "재료 등록 방법 문의") String title,
        @Schema(description = "문의 내용", example = "냉장고에 재료를 어떻게 등록하나요?") String content,
        @Schema(description = "작성 회원 닉네임", example = "냉파초보") String nickname,
        @Schema(description = "답변 완료 여부", example = "true") Boolean isAnswered,
        @Schema(description = "문의 등록 시각", example = "2026-08-11T09:00:00") LocalDateTime createdAt,
        @Schema(description = "답변 ID", example = "3", nullable = true) Long answerId,
        @Schema(description = "답변 내용", example = "냉장고 메뉴에서 재료 추가를 선택해 주세요.", nullable = true) String answerContent,
        @Schema(description = "답변 관리자 ID", example = "2", nullable = true) Long answeredBy,
        @Schema(description = "답변 등록 시각", example = "2026-08-11T10:00:00", nullable = true) LocalDateTime answeredAt
) {

    public static AdminInquiryDetailResponse from(Inquiry inquiry, InquiryAnswer inquiryAnswer, String nickname) {
        return new AdminInquiryDetailResponse(
                inquiry.getId(),
                inquiry.getMemberId(),
                inquiry.getTitle(),
                inquiry.getContent(),
                nickname,
                inquiry.getIsAnswered(),
                inquiry.getCreatedAt(),
                inquiryAnswer != null ? inquiryAnswer.getId() : null,
                inquiryAnswer != null ? inquiryAnswer.getContent() : null,
                inquiryAnswer != null ? inquiryAnswer.getCreatedBy() : null,
                inquiryAnswer != null ? inquiryAnswer.getCreatedAt() : null
        );
    }
}
