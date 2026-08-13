package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePost;
import com.naengpa.naengpamasterbackend.community.share.entity.CommunitySharePostStatus;

import java.time.LocalDateTime;

public record AdminCommunitySharePostResponse(
        Long communitySharePostId,
        Long ownerMemberId,
        String ownerNickname,
        String ownerEmail,
        String title,
        Long productId,
        String ingredientName,
        String quantity,
        Integer totalPrice,
        Integer participantLimit,
        Integer joinedCount,
        String address,
        String description,
        CommunitySharePostStatus status,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {
    public static AdminCommunitySharePostResponse from(
            CommunitySharePost post,
            String ownerNickname,
            String ownerEmail,
            int joinedCount
    ) {
        return new AdminCommunitySharePostResponse(
                post.getCommunitySharePostId(),
                post.getMemberId(),
                ownerNickname,
                ownerEmail,
                post.getTitle(),
                post.getProductId(),
                post.getIngredientName(),
                post.getQuantity(),
                post.getTotalPrice(),
                post.getParticipantLimit(),
                joinedCount,
                post.getAddress(),
                post.getDescription(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getClosedAt()
        );
    }
}
