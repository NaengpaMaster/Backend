package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminProductRepository extends JpaRepository<Product, Long> {
    @Query("""
            SELECT p
            FROM Product p
            WHERE :search = ''
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR p.productCategoryId IN (
                    SELECT c.productCategoryId
                    FROM ProductCategory c
                    WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
               )
            """)
    Page<Product> findProducts(@Param("search") String search, Pageable pageable);

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
