package com.naengpa.naengpamasterbackend.score.dto.response;

import com.naengpa.naengpamasterbackend.score.entity.ScoreReason;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "냉파 점수 변경 이력 응답")
public record ScoreHistoryResponse(

        @Schema(
                description = "점수 변경 사유",
                example = "EXPIRED_PRODUCT"
        )
        ScoreReason scoreReason,

        @Schema(
                description = "점수 변경 대상명",
                example = "우유",
                nullable = true
        )
        String targetName,

        @Schema(
                description = "식재료 카테고리 ID",
                example = "3",
                nullable = true
        )
        Long productCategoryId,

        @Schema(description = "점수 증감값", example = "-2")
        Integer scoreDelta,

        @Schema(
                description = "점수 이력 생성 일시",
                example = "2026-08-12T14:30:00"
        )
        LocalDateTime createdAt

) {}