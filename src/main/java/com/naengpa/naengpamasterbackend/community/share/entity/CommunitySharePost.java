package com.naengpa.naengpamasterbackend.community.share.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_share_posts")
@Getter
@NoArgsConstructor
public class CommunitySharePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_share_post_id")
    private Long communitySharePostId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "ingredient_name", nullable = false, length = 100)
    private String ingredientName;

    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, length = 100)
    private String quantity;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String address;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunitySharePostStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public static CommunitySharePost create(
            Long memberId,
            String title,
            String ingredientName,
            Long productId,
            String quantity,
            Integer totalPrice,
            Integer participantLimit,
            BigDecimal latitude,
            BigDecimal longitude,
            String address,
            String description
    ) {
        CommunitySharePost post = new CommunitySharePost();
        post.memberId = memberId;
        post.title = title;
        post.ingredientName = ingredientName;
        post.productId = productId;
        post.quantity = quantity;
        post.totalPrice = totalPrice;
        post.participantLimit = participantLimit;
        post.latitude = latitude;
        post.longitude = longitude;
        post.address = address;
        post.description = description;
        post.status = CommunitySharePostStatus.OPEN;
        return post;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getCommunitySharePostId() {
        return communitySharePostId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getTitle() {
        return title;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public Long getProductId() {
        return productId;
    }

    public String getQuantity() {
        return quantity;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public Integer getParticipantLimit() {
        return participantLimit;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public CommunitySharePostStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public boolean isOpen() {
        return status == CommunitySharePostStatus.OPEN;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public int calculateSharePrice() {
        return (int) Math.ceil((double) totalPrice / participantLimit);
    }

    public void close() {
        status = CommunitySharePostStatus.CLOSED;
        closedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        status = CommunitySharePostStatus.CANCELLED;
        closedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}
