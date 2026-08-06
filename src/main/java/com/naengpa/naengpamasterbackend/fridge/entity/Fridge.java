package com.naengpa.naengpamasterbackend.fridge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fridges")
@Getter
@NoArgsConstructor
public class Fridge {

    private static final String ACTIVE_STATUS = "ACTIVE";

    @Id
    @Column(name = "fridge_id")
    private Long fridgeId;

    @Column(name = "owner_member_id", nullable = false)
    private Long ownerMemberId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Fridge create(Long fridgeId, Long ownerMemberId, String ownerNickname) {
        Fridge fridge = new Fridge();
        fridge.fridgeId = fridgeId;
        fridge.ownerMemberId = ownerMemberId;
        fridge.name = ownerNickname + "의 냉장고";
        fridge.status = ACTIVE_STATUS;
        return fridge;
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
