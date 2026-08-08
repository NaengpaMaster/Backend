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
@Table(name = "fridge_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FridgeMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_member_id")
    private Long fridgeMemberId;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FridgeMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FridgeMemberStatus status = FridgeMemberStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    public static FridgeMember createOwner(Long fridgeId, Long memberId) {
        FridgeMember fridgeMember = new FridgeMember();
        fridgeMember.fridgeId = fridgeId;
        fridgeMember.memberId = memberId;
        fridgeMember.role = FridgeMemberRole.OWNER;
        fridgeMember.status = FridgeMemberStatus.ACTIVE;
        return fridgeMember;
    }

    public static FridgeMember createMember(Long fridgeId, Long memberId) {
        FridgeMember fridgeMember = new FridgeMember();
        fridgeMember.fridgeId = fridgeId;
        fridgeMember.memberId = memberId;
        fridgeMember.role = FridgeMemberRole.MEMBER;
        fridgeMember.status = FridgeMemberStatus.ACTIVE;
        return fridgeMember;
    }

    public boolean isOwner() {
        return role == FridgeMemberRole.OWNER;
    }

    public void activateAsMember() {
        role = FridgeMemberRole.MEMBER;
        status = FridgeMemberStatus.ACTIVE;
        leftAt = null;
    }

    public void leave() {
        status = FridgeMemberStatus.INACTIVE;
        leftAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        joinedAt = LocalDateTime.now();
    }
}
