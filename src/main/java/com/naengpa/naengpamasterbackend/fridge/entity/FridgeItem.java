package com.naengpa.naengpamasterbackend.fridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fridge_items")
@Getter
@NoArgsConstructor
public class FridgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fridge_item_id")
    private Long fridgeItemId;

    @Column(name = "fridge_id", nullable = false)
    private Long fridgeId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "quantity", nullable = false, length = 100)
    private String quantity;
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    @Column(name = "memo", length = 1000)
    private String memo;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    //생성
    public static FridgeItem create(
            Long fridgeId,
            Long memberId,
            Long productId,
            String quantity,
            LocalDate expiryDate,
            String memo
    ) {
        FridgeItem fridgeItem = new FridgeItem();
        fridgeItem.fridgeId = fridgeId;
        fridgeItem.memberId = memberId;
        fridgeItem.productId = productId;
        fridgeItem.quantity = quantity;
        fridgeItem.expiryDate = expiryDate;
        fridgeItem.memo = memo;
        fridgeItem.isDeleted = false;
        return fridgeItem;

    }

    //Db insert 되기 전 실행되는 메서드
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        isDeleted = false;
    }

    //추가
    public void update(
            Long productId,
            String quantity,
            LocalDate expiryDate,
            String memo
    ) {
        this.productId = productId;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.memo = memo;
        this.updatedAt = LocalDateTime.now();
    }

    //삭제
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    //전부 사용
    public void useAll() {
        delete();
    }

    //일부 사용
    public void usePartial(String quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }

}
