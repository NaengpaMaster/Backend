package com.naengpa.naengpamasterbackend.admin;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminProductCreateRequest;
import com.naengpa.naengpamasterbackend.admin.dto.request.AdminProductUpdateRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminProductResponse;
import com.naengpa.naengpamasterbackend.admin.service.AdminProductService;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateProductNameException;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest
@Transactional
class AdminProductServiceTests {

    @Autowired
    private AdminProductService adminProductService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("전체 사전 재료 조회 시 활성/비활성 재료를 모두 반환")
    void findAllProducts_returnsActiveAndInactiveProducts() {
        // given
        String activeName = "테스트활성재료" + System.nanoTime();
        String inactiveName = "테스트비활성재료" + System.nanoTime();

        jdbcTemplate.update("""
                INSERT INTO products (product_category_id, name, default_expiry_days, is_active)
                VALUES (1, ?, 7, true)
                """, activeName);

        jdbcTemplate.update("""
                INSERT INTO products (product_category_id, name, default_expiry_days, is_active)
                VALUES (1, ?, 7, false)
                """, inactiveName);

        // when
        List<AdminProductResponse> result = adminProductService
                .findAllProducts("", Pageable.unpaged())
                .content();

        // then
        assertThat(result)
                .extracting(AdminProductResponse::name)
                .contains(activeName, inactiveName);
    }

    @Test
    @DisplayName("비활성 사전 재료 조회 시 비활성 재료만 반환")
    void findInactiveProducts_returnsOnlyInactiveProducts() {
        // given
        String activeName = "테스트활성재료" + System.nanoTime();
        String inactiveName = "테스트비활성재료" + System.nanoTime();

        jdbcTemplate.update("""
                INSERT INTO products (product_category_id, name, default_expiry_days, is_active)
                VALUES (1, ?, 7, true)
                """, activeName);

        jdbcTemplate.update("""
                INSERT INTO products (product_category_id, name, default_expiry_days, is_active)
                VALUES (1, ?, 7, false)
                """, inactiveName);

        // when
        List<AdminProductResponse> result = adminProductService.findInactiveProducts();

        // then
        assertThat(result)
                .extracting(AdminProductResponse::name)
                .contains(inactiveName)
                .doesNotContain(activeName);

        assertThat(result)
                .extracting(AdminProductResponse::isActive)
                .containsOnly(false);
    }

    @Test
    @DisplayName("사전 재료 추가 시 AdminProductResponse를 반환")
    void createProduct_returnsAdminProductResponse() {
        // given
        String name = "테스트재료" + System.nanoTime();

        AdminProductCreateRequest request = new AdminProductCreateRequest(
                1L,
                name,
                3
        );

        // when
        AdminProductResponse result = adminProductService.createProduct(request);

        // then
        assertThat(result.productId()).isNotNull();
        assertThat(result.productCategoryId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo(name);
        assertThat(result.defaultExpiryDays()).isEqualTo(3);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("중복된 사전 재료명으로 추가 시 DuplicateProductNameException 발생")
    void createProduct_throwsDuplicateProductNameExceptionWhenNameDuplicated() {
        // given
        String name = "중복재료" + System.nanoTime();

        jdbcTemplate.update("""
                INSERT INTO products (product_category_id, name, default_expiry_days, is_active)
                VALUES (1, ?, 3, true)
                """, name);

        AdminProductCreateRequest request = new AdminProductCreateRequest(
                1L,
                name,
                3
        );

        // when
        Throwable thrown = catchThrowable(() -> adminProductService.createProduct(request));

        // then
        assertThat(thrown).isInstanceOf(DuplicateProductNameException.class);
    }

    @Test
    @DisplayName("사전 재료 수정 시 AdminProductResponse를 반환")
    void updateProduct_returnsAdminProductResponse() {
        // given
        String originalName = "수정전재료" + System.nanoTime();
        String updatedName = "수정후재료" + System.nanoTime();

        AdminProductCreateRequest createRequest = new AdminProductCreateRequest(
                1L,
                originalName,
                3
        );

        AdminProductResponse created = adminProductService.createProduct(createRequest);

        AdminProductUpdateRequest updateRequest = new AdminProductUpdateRequest(
                1L,
                updatedName,
                5
        );

        // when
        AdminProductResponse result = adminProductService.updateProduct(
                created.productId(),
                updateRequest
        );

        // then
        assertThat(result.productId()).isEqualTo(created.productId());
        assertThat(result.productCategoryId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo(updatedName);
        assertThat(result.defaultExpiryDays()).isEqualTo(5);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("없는 사전 재료 수정 시 ProductNotFoundException 발생")
    void updateProduct_throwsProductNotFoundExceptionWhenProductNotFound() {
        // given
        Long productId = 999999999L;

        AdminProductUpdateRequest request = new AdminProductUpdateRequest(
                1L,
                "없는재료수정",
                5
        );

        // when
        Throwable thrown = catchThrowable(() -> adminProductService.updateProduct(productId, request));

        // then
        assertThat(thrown).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("다른 사전 재료가 사용 중인 이름으로 수정 시 DuplicateProductNameException 발생")
    void updateProduct_throwsDuplicateProductNameExceptionWhenNameDuplicated() {
        // given
        String firstName = "첫번째재료" + System.nanoTime();
        String secondName = "두번째재료" + System.nanoTime();

        AdminProductResponse first = adminProductService.createProduct(
                new AdminProductCreateRequest(1L, firstName, 3)
        );

        AdminProductResponse second = adminProductService.createProduct(
                new AdminProductCreateRequest(1L, secondName, 3)
        );

        AdminProductUpdateRequest request = new AdminProductUpdateRequest(
                1L,
                second.name(),
                5
        );

        // when
        Throwable thrown = catchThrowable(() -> adminProductService.updateProduct(first.productId(), request));

        // then
        assertThat(thrown).isInstanceOf(DuplicateProductNameException.class);
    }

    @Test
    @DisplayName("사전 재료 비활성화 시 isActive가 false로 변경")
    void deactivateProduct_changesIsActiveFalse() {
        // given
        String name = "비활성화재료" + System.nanoTime();

        AdminProductResponse created = adminProductService.createProduct(
                new AdminProductCreateRequest(1L, name, 3)
        );

        // when
        AdminProductResponse result = adminProductService.deactivateProduct(created.productId());

        // then
        assertThat(result.productId()).isEqualTo(created.productId());
        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("사전 재료 재활성화 시 isActive가 true로 변경")
    void activateProduct_changesIsActiveTrue() {
        // given
        String name = "재활성화재료" + System.nanoTime();

        AdminProductResponse created = adminProductService.createProduct(
                new AdminProductCreateRequest(1L, name, 3)
        );

        adminProductService.deactivateProduct(created.productId());

        // when
        AdminProductResponse result = adminProductService.activateProduct(created.productId());

        // then
        assertThat(result.productId()).isEqualTo(created.productId());
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("없는 사전 재료 비활성화 시 ProductNotFoundException 발생")
    void deactivateProduct_throwsProductNotFoundExceptionWhenProductNotFound() {
        // given
        Long productId = 999999999L;

        // when
        Throwable thrown = catchThrowable(() -> adminProductService.deactivateProduct(productId));

        // then
        assertThat(thrown).isInstanceOf(ProductNotFoundException.class);
    }

}
