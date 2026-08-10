package com.naengpa.naengpamasterbackend.fridge.repository;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FridgeItemHistoryRepository extends JpaRepository<FridgeItemHistory, Long> {
}
