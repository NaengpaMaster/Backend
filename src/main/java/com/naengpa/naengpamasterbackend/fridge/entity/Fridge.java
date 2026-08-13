package com.naengpa.naengpamasterbackend.fridge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fridges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_id")
    private Long fridgeId;

    @Column(name = "owner_member_id", nullable = false)
    private Long ownerMemberId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FridgeStatus status = FridgeStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Fridge createDefault(Long ownerMemberId, String nickname) {
        Fridge fridge = new Fridge();
        fridge.ownerMemberId = ownerMemberId;
        fridge.name = resolveDefaultName(nickname);
        fridge.status = FridgeStatus.ACTIVE;
        return fridge;
    }

    public void updateName(String name) {
        this.name = name;
    }

    private static String resolveDefaultName(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "기본의 냉장고";
        }
        return nickname.trim() + "의 냉장고";
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
