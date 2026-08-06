package com.naengpa.naengpamasterbackend.fridge.service;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeRepository;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FridgeService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final FridgeRepository fridgeRepository;

    public FridgeService(FridgeRepository fridgeRepository) {
        this.fridgeRepository = fridgeRepository;
    }

    @Transactional
    public synchronized Long findOrCreateFridgeId(Member member) {
        return fridgeRepository.findFirstByOwnerMemberIdAndStatusOrderByFridgeIdAsc(member.getId(), ACTIVE_STATUS)
                .map(Fridge::getFridgeId)
                .orElseGet(() -> createFridge(member).getFridgeId());
    }

    private Fridge createFridge(Member member) {
        Long nextFridgeId = fridgeRepository.findMaxFridgeId() + 1;
        return fridgeRepository.save(Fridge.create(nextFridgeId, member.getId(), member.getNickname()));
    }
}
