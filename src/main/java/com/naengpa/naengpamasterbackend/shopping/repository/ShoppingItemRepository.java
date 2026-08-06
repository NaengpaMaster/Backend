package com.naengpa.naengpamasterbackend.shopping.repository;

import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItem;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {


    List<ShoppingItem> findByMemberIdAndIsDeletedFalse(Long memberId);

    Optional<ShoppingItem> findByShoppingItemIdAndMemberIdAndIsDeletedFalse(
            Long shoppingItemId,
            Long memberId
    );

    boolean existsByMemberIdAndProductIdAndIsDeletedFalseAndIsPurchasedFalse(
            Long memberId,
            Long productId
    );

}
