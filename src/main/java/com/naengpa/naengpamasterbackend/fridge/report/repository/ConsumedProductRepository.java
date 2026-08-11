package com.naengpa.naengpamasterbackend.fridge.report.repository;

import com.naengpa.naengpamasterbackend.fridge.report.entity.ConsumedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsumedProductRepository extends JpaRepository<ConsumedProduct, Long> {

    List<ConsumedProduct> findAllByFridgeIdAndConsumedAtGreaterThanEqualAndConsumedAtLessThan(
            Long fridgeId,
            LocalDateTime startAt,
            LocalDateTime endAt
    );
}
