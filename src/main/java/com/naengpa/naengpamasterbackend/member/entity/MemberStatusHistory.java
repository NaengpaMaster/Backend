package com.naengpa.naengpamasterbackend.member.entity;

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
@Table(name = "member_status_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_status_history_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 20)
    private MemberStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "changed_status", nullable = false, length = 20)
    private MemberStatus changedStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public static MemberStatusHistory create(
            Long memberId,
            MemberStatus previousStatus,
            MemberStatus changedStatus
    ) {
        MemberStatusHistory history = new MemberStatusHistory();
        history.memberId = memberId;
        history.previousStatus = previousStatus;
        history.changedStatus = changedStatus;
        return history;
    }
}
