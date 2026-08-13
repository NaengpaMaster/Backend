package com.naengpa.naengpamasterbackend.inquiry.dto.response;

import com.naengpa.naengpamasterbackend.inquiry.entity.Inquiry;
import com.naengpa.naengpamasterbackend.inquiry.entity.InquiryAnswer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record InquiryDetailResponse(
        @Schema(description = "문의 ID", example = "1")
        Long inquiryId,
        @Schema(description = "문의 제목", example = "재료 등록 방법 문의")
        String title,
        @Schema(description = "문의 내용", example = "냉장고에 재료를 어떻게 등록하나요?")
        String content,
        @Schema(description = "답변 완료 여부", example = "true")
        Boolean isAnswered,
        @Schema(description = "문의 등록 시각", example = "2026-08-11T09:00:00")
        LocalDateTime createdAt,
        @Schema(description = "답변 내용", example = "냉장고 메뉴에서 재료 추가를 선택해 주세요.", nullable = true)
        String answerContent,
        @Schema(description = "답변 등록 시각", example = "2026-08-11T10:00:00", nullable = true)
        LocalDateTime answeredAt
) {

    public static InquiryDetailResponse from(Inquiry inquiry, InquiryAnswer inquiryAnswer) {
        return new InquiryDetailResponse(
                inquiry.getId(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getIsAnswered(),
                inquiry.getCreatedAt(),
                inquiryAnswer != null ? inquiryAnswer.getContent() : null,
                inquiry.getAnsweredAt()
        );
    }
}
