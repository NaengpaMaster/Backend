package com.naengpa.naengpamasterbackend.shopping.repository;

import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingItemHistoryRepository extends JpaRepository<ShoppingItemHistory, Long> {
}
