package com.naengpa.naengpamasterbackend.fridge.repository;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    Optional<Fridge> findFirstByOwnerMemberIdAndStatusOrderByFridgeIdAsc(Long ownerMemberId, String status);

    @Query("select coalesce(max(f.fridgeId), 0) from Fridge f")
    Long findMaxFridgeId();
}
