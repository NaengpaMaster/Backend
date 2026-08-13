package com.naengpa.naengpamasterbackend.product.repository;

import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.quiz.dto.ProductNameId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsActiveTrueAndNameContaining(String keyword);

    boolean existsByProductIdAndIsActiveTrue(Long productId);

    List<Product> findByProductIdInAndIsActiveTrue(List<Long> productIds);

    List<Product> findByProductCategoryId(Long productCategoryId);

    List<Product> findByProductIdIn(List<Long> productIds);

    List<Product> findByIsActiveTrue();

    // exact match용
    Optional<Product> findByNameAndIsActiveTrue(String name);
  
    @Query("SELECT new com.naengpa.naengpamasterbackend.quiz.dto.ProductNameId(p.productId, p.name) " +
            "FROM Product p WHERE p.isActive = true")
    List<ProductNameId> findAllActiveProductNameIds();

}
