package com.naengpa.naengpamasterbackend.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "billing_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BillingKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_key_id")
    private Long billingKeyId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "toss_customer_key", nullable = false, unique = true)
    private String tossCustomerKey;

    @Column(name = "toss_billing_key", nullable = false, unique = true)
    private String tossBillingKey;

    @Column(name = "card_company", length = 100)
    private String cardCompany;

    @Column(name = "card_number_masked", length = 50)
    private String cardNumberMasked;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    public static BillingKey create(
            Long memberId,
            String tossCustomerKey,
            String tossBillingKey,
            String cardCompany,
            String cardNumberMasked
    ) {
        BillingKey billingKey = new BillingKey();
        billingKey.memberId = memberId;
        billingKey.tossCustomerKey = tossCustomerKey;
        billingKey.tossBillingKey = tossBillingKey;
        billingKey.cardCompany = cardCompany;
        billingKey.cardNumberMasked = cardNumberMasked;
        billingKey.isActive = true;
        return billingKey;
    }

    public void deactivate() {
        this.isActive = false;
        this.deactivatedAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}