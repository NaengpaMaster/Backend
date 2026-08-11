package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminProductCreateRequest;
import com.naengpa.naengpamasterbackend.admin.dto.request.AdminProductUpdateRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminProductResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminProductPageResponse;
import com.naengpa.naengpamasterbackend.admin.repository.AdminProductRepository;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateProductNameException;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import com.naengpa.naengpamasterbackend.product.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class AdminProductService {

    private final AdminProductRepository adminProductRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public AdminProductService(
            AdminProductRepository adminProductRepository,
            ProductCategoryRepository productCategoryRepository
    ) {
        this.adminProductRepository = adminProductRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    private void validateCategory(Long productCategoryId) {
        if (!productCategoryRepository.existsById(productCategoryId)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }
    }

    //어드민 사전 재료 전체 조회
    @Transactional(readOnly = true)
    public AdminProductPageResponse findAllProducts(String search, Pageable pageable) {
        Page<AdminProductResponse> products = adminProductRepository
                .findProducts(search == null ? "" : search.trim(), pageable)
                .map(AdminProductResponse::from);

        return AdminProductPageResponse.from(
                products,
                adminProductRepository.count(),
                adminProductRepository.countByIsActiveTrue()
        );
    }

    //어드민 사전 재료 비활성 조회
    public List<AdminProductResponse> findInactiveProducts() {
        return adminProductRepository.findByIsActiveFalseOrderByProductIdAsc().stream()
                .map(AdminProductResponse::from)
                .toList();
    }

    //어드민 사전 재료 추가
    public AdminProductResponse createProduct(AdminProductCreateRequest request) {

        validateCategory(request.productCategoryId());

        if (adminProductRepository.existsByName(request.name())) {
            throw new DuplicateProductNameException();
        }

        Product product = Product.create(
                request.productCategoryId(),
                request.name(),
                request.defaultExpiryDays()
        );

        Product savedProduct = adminProductRepository.save(product);

        return AdminProductResponse.from(savedProduct);
    }

    //어드민 사전 재료 수정
    @Transactional
    public AdminProductResponse updateProduct(Long productId, AdminProductUpdateRequest request) {

        Product product = adminProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        validateCategory(request.productCategoryId());

        if (!product.getName().equals(request.name())
                && adminProductRepository.existsByName(request.name())) {
            throw new DuplicateProductNameException();
        }

        product.update(
                request.productCategoryId(),
                request.name(),
                request.defaultExpiryDays()
        );

        return AdminProductResponse.from(product);
    }

    // 어드민 사전 재료 비활성화
    @Transactional
    public AdminProductResponse deactivateProduct(Long productId) {
        Product product = adminProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.deactivate();

        return AdminProductResponse.from(product);
    }

    // 어드민 사전 재료 재활성화
    @Transactional
    public AdminProductResponse activateProduct(Long productId) {
        Product product = adminProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.activate();

        return AdminProductResponse.from(product);
    }


}
