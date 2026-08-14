package com.naengpa.naengpamasterbackend.community.share.dto.response;

import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePost;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunityShareParticipantStatus;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePostStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CommunitySharePostResponse(
        Long communitySharePostId,
        Long ownerMemberId,
        String ownerNickname,
        boolean mine,
        String title,
        Long productId,
        String ingredientName,
        String quantity,
        Integer totalPrice,
        Integer participantLimit,
        Integer sharePrice,
        Integer joinedCount,
        Integer remainingSlots,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String description,
        CommunitySharePostStatus status,
        Double distanceKm,
        boolean joined,
        LocalDateTime createdAt
) {
    public static CommunitySharePostResponse from(
            CommunitySharePost post,
            String ownerNickname,
            Long currentMemberId,
            int joinedCount,
            boolean joined,
            Double distanceKm
    ) {
        return new CommunitySharePostResponse(
                post.getCommunitySharePostId(),
                post.getMemberId(),
                ownerNickname,
                post.isOwnedBy(currentMemberId),
                post.getTitle(),
                post.getProductId(),
                post.getIngredientName(),
                post.getQuantity(),
                post.getTotalPrice(),
                post.getParticipantLimit(),
                post.calculateSharePrice(),
                joinedCount,
                Math.max(post.getParticipantLimit() - joinedCount, 0),
                post.getLatitude(),
                post.getLongitude(),
                post.getAddress(),
                post.getDescription(),
                post.getStatus(),
                distanceKm,
                joined,
                post.getCreatedAt()
        );
    }
}
