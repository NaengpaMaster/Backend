package com.naengpa.naengpamasterbackend.fridge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fridge_invites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_invite_id")
    private Long fridgeInviteId;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "inviter_member_id", nullable = false)
    private Long inviterMemberId;

    @Column(name = "invitee_member_id", nullable = false)
    private Long inviteeMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FridgeInviteStatus status = FridgeInviteStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public static FridgeInvite create(Long fridgeId, Long inviterMemberId, Long inviteeMemberId) {
        FridgeInvite invite = new FridgeInvite();
        invite.fridgeId = fridgeId;
        invite.inviterMemberId = inviterMemberId;
        invite.inviteeMemberId = inviteeMemberId;
        invite.status = FridgeInviteStatus.PENDING;
        return invite;
    }

    public void accept() {
        status = FridgeInviteStatus.ACCEPTED;
        respondedAt = LocalDateTime.now();
    }

    public void reject() {
        status = FridgeInviteStatus.REJECTED;
        respondedAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
