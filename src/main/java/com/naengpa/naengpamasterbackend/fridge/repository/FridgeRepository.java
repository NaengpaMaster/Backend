package com.naengpa.naengpamasterbackend.fridge.repository;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    Optional<Fridge> findFirstByOwnerMemberIdAndStatusOrderByFridgeIdAsc(Long ownerMemberId, FridgeStatus status);

    List<Fridge> findAllByOwnerMemberIdAndStatus(Long ownerMemberId, FridgeStatus status);
}
