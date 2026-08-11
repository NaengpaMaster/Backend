package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminProductRepository extends JpaRepository<Product, Long> {
    // 전체 사전 재료를 ID 오름차순으로 조회합니다.
    List<Product> findAllByOrderByProductIdAsc();

    // 비활성 사전 재료를 ID 오름차순으로 조회합니다.
    List<Product> findByIsActiveFalseOrderByProductIdAsc();

    // 같은 이름의 사전 재료가 존재하는지 확인합니다.
    boolean existsByName(String name);

    // 활성 사전 재료 수를 조회합니다.
    long countByIsActiveTrue();

    // 비활성 사전 재료 수를 조회합니다.
    long countByIsActiveFalse();
}
