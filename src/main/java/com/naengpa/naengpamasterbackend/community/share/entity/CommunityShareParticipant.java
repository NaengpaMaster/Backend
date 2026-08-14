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

import java.time.LocalDateTime;

@Entity
@Table(name = "community_share_participants")
@Getter
@NoArgsConstructor
public class CommunityShareParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_share_participant_id")
    private Long communityShareParticipantId;

    @Column(name = "community_share_post_id", nullable = false)
    private Long communitySharePostId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityShareParticipantStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static CommunityShareParticipant join(Long communitySharePostId, Long memberId) {
        CommunityShareParticipant participant = new CommunityShareParticipant();
        participant.communitySharePostId = communitySharePostId;
        participant.memberId = memberId;
        participant.status = CommunityShareParticipantStatus.JOINED;
        return participant;
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

    public boolean isJoined() {
        return status == CommunityShareParticipantStatus.JOINED;
    }

    public void cancel() {
        status = CommunityShareParticipantStatus.CANCELLED;
        updatedAt = LocalDateTime.now();
    }

    public void rejoin() {
        status = CommunityShareParticipantStatus.JOINED;
        updatedAt = LocalDateTime.now();
    }
}
