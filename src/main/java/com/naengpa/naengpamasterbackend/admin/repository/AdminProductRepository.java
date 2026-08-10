package com.naengpa.naengpamasterbackend.admin.repository;

import com.naengpa.naengpamasterbackend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByProductIdAsc();

    List<Product> findByIsActiveFalseOrderByProductIdAsc();

    boolean existsByName(String name);

    long countByIsActiveTrue();

    long countByIsActiveFalse();
}
