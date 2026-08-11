package com.naengpa.naengpamasterbackend.receipt;

import com.naengpa.naengpamasterbackend.global.exception.InvalidReceiptImageException;
import com.naengpa.naengpamasterbackend.global.s3.S3Uploader;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemCreateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeItemService;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptOcrItemRequest;
import com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptOcrSaveRequest;
import com.naengpa.naengpamasterbackend.receipt.dto.response.ReceiptAnalysisItemResponse;
import com.naengpa.naengpamasterbackend.receipt.dto.response.ReceiptImageUploadResponse;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysis;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisItem;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisItemStatus;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisStatus;
import com.naengpa.naengpamasterbackend.receipt.repository.ReceiptAnalysisItemRepository;
import com.naengpa.naengpamasterbackend.receipt.repository.ReceiptAnalysisRepository;
import com.naengpa.naengpamasterbackend.receipt.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReceiptServiceTests {

    private MemberRepository memberRepository;
    private ReceiptAnalysisRepository receiptAnalysisRepository;
    private ReceiptAnalysisItemRepository receiptAnalysisItemRepository;
    private ProductRepository productRepository;
    private FridgeItemService fridgeItemService;
    private S3Uploader s3Uploader;
    private ReceiptService receiptService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        receiptAnalysisRepository = mock(ReceiptAnalysisRepository.class);
        receiptAnalysisItemRepository = mock(ReceiptAnalysisItemRepository.class);
        productRepository = mock(ProductRepository.class);
        fridgeItemService = mock(FridgeItemService.class);
        s3Uploader = mock(S3Uploader.class);
        receiptService = new ReceiptService(
                memberRepository,
                receiptAnalysisRepository,
                s3Uploader,
                receiptAnalysisItemRepository,
                productRepository,
                fridgeItemService
        );
    }

    @Test
    @DisplayName("영수증 이미지 업로드 성공 시 S3에 저장하고 PENDING 분석 row를 생성한다")
    void uploadReceiptImage_success() {
        Member member = Member.createUser("user@test.com", "encoded", "사용자", null);
        ReflectionTestUtils.setField(member, "id", 7L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receipt.JPG",
                "image/jpeg",
                "receipt-image".getBytes()
        );

        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));
        when(s3Uploader.upload(any(MultipartFile.class), anyString()))
                .thenReturn("receipts/7/test-object.jpg");
        when(receiptAnalysisRepository.save(any(ReceiptAnalysis.class)))
                .thenAnswer(invocation -> {
                    ReceiptAnalysis receiptAnalysis = invocation.getArgument(0);
                    ReflectionTestUtils.setField(receiptAnalysis, "receiptAnalysisId", 1L);
                    return receiptAnalysis;
                });

        ReceiptImageUploadResponse response = receiptService.uploadReceiptImage("user@test.com", file);

        assertThat(response.receiptAnalysisId()).isEqualTo(1L);
        assertThat(response.originalFileName()).isEqualTo("receipt.JPG");
        assertThat(response.status()).isEqualTo(ReceiptAnalysisStatus.PENDING);
        verify(s3Uploader).upload(any(MultipartFile.class), anyString());
        verify(receiptAnalysisRepository).save(any(ReceiptAnalysis.class));
    }

    @Test
    @DisplayName("빈 파일이면 업로드하지 않고 400 대상 예외를 발생시킨다")
    void uploadReceiptImage_emptyFile() {
        Member member = Member.createUser("user@test.com", "encoded", "사용자", null);
        ReflectionTestUtils.setField(member, "id", 7L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receipt.jpg",
                "image/jpeg",
                new byte[0]
        );

        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> receiptService.uploadReceiptImage("user@test.com", file))
                .isInstanceOf(InvalidReceiptImageException.class)
                .hasMessage("영수증 이미지를 업로드해주세요.");

        verify(s3Uploader, never()).upload(any(MultipartFile.class), anyString());
        verify(receiptAnalysisRepository, never()).save(any(ReceiptAnalysis.class));
    }

    @Test
    @DisplayName("허용하지 않는 확장자면 업로드하지 않고 400 대상 예외를 발생시킨다")
    void uploadReceiptImage_invalidExtension() {
        Member member = Member.createUser("user@test.com", "encoded", "사용자", null);
        ReflectionTestUtils.setField(member, "id", 7L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receipt.pdf",
                "application/pdf",
                "receipt-pdf".getBytes()
        );

        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> receiptService.uploadReceiptImage("user@test.com", file))
                .isInstanceOf(InvalidReceiptImageException.class)
                .hasMessage("영수증 이미지는 jpg, jpeg, png 파일만 업로드할 수 있습니다.");

        verify(s3Uploader, never()).upload(any(MultipartFile.class), anyString());
        verify(receiptAnalysisRepository, never()).save(any(ReceiptAnalysis.class));
    }

    @Test
    @DisplayName("10MB 초과 파일이면 업로드하지 않고 400 대상 예외를 발생시킨다")
    void uploadReceiptImage_overSize() {
        Member member = Member.createUser("user@test.com", "encoded", "사용자", null);
        ReflectionTestUtils.setField(member, "id", 7L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receipt.png",
                "image/png",
                new byte[(10 * 1024 * 1024) + 1]
        );

        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> receiptService.uploadReceiptImage("user@test.com", file))
                .isInstanceOf(InvalidReceiptImageException.class)
                .hasMessage("영수증 이미지는 10MB 이하만 업로드할 수 있습니다.");

        verify(s3Uploader, never()).upload(any(MultipartFile.class), anyString());
        verify(receiptAnalysisRepository, never()).save(any(ReceiptAnalysis.class));
    }

    @Test
    @DisplayName("OCR 결과 저장 시 사전 재료와 매칭된 후보만 PENDING 상태로 저장한다")
    void saveOcrResult_savesOnlyMatchedItems() {
        Member member = Member.createUser("user@test.com", "encoded", "사용자", null);
        ReflectionTestUtils.setField(member, "id", 7L);

        ReceiptAnalysis receiptAnalysis = ReceiptAnalysis.createPending(
                7L,
                "receipt.jpeg",
                "receipts/7/test.jpeg"
        );
        ReflectionTestUtils.setField(receiptAnalysis, "receiptAnalysisId", 1L);

        Product mushroom = Product.create(1L, "팽이버섯", 3);
        ReflectionTestUtils.setField(mushroom, "productId", 10L);

        Product tofu = Product.create(1L, "두부", 5);
        ReflectionTestUtils.setField(tofu, "productId", 20L);

        ReceiptOcrSaveRequest request = new ReceiptOcrSaveRequest(
                "007 국산 팽이버섯 200g 1 1000",
                List.of(
                        new ReceiptOcrItemRequest("국산 팽이버섯 200g", "1개"),
                        new ReceiptOcrItemRequest("강릉최가두부 550g", "2개"),
                        new ReceiptOcrItemRequest("유상봉투", "1개")
                )
        );

        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));
        when(receiptAnalysisRepository.findByReceiptAnalysisIdAndMemberId(1L, 7L))
                .thenReturn(Optional.of(receiptAnalysis));
        when(productRepository.findByNameAndIsActiveTrue("팽이버섯"))
                .thenReturn(Optional.of(mushroom));
        when(productRepository.findByNameAndIsActiveTrue("강릉최가두부"))
                .thenReturn(Optional.empty());
        when(productRepository.findByNameAndIsActiveTrue("유상봉투"))
                .thenReturn(Optional.empty());
        when(productRepository.findByIsActiveTrue())
                .thenReturn(List.of(mushroom, tofu));

        List<ReceiptAnalysisItemResponse> response = receiptService.saveOcrResult(
                "user@test.com",
                1L,
                request
        );

        assertThat(receiptAnalysis.getRawOcrText()).isEqualTo("007 국산 팽이버섯 200g 1 1000");
        assertThat(response)
                .extracting(ReceiptAnalysisItemResponse::matchedProductName)
                .containsExactly("팽이버섯", "두부");
        assertThat(response)
                .extracting(ReceiptAnalysisItemResponse::expiryDate)
                .containsExactly(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));

        // 매칭 실패한 유상봉투는 저장 후보에서 제외되는지 확인
        ArgumentCaptor<List<ReceiptAnalysisItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(receiptAnalysisItemRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("PENDING OCR 후보의 재료와 수량을 수정한다")
    void updateReceiptAnalysisItem_success() {
        Member member = Member.createUser("user@test.com", "encoded", "사용자", null);
        ReflectionTestUtils.setField(member, "id", 7L);

        ReceiptAnalysisItem item = ReceiptAnalysisItem.createPending(
                1L,
                10L,
                "국산 팽이버섯",
                "팽이버섯",
                "팽이버섯",
                "1개",
                LocalDate.now().plusDays(3)
        );
        ReflectionTestUtils.setField(item, "receiptAnalysisItemId", 11L);

        Product tofu = Product.create(1L, "두부", 5);
        ReflectionTestUtils.setField(tofu, "productId", 20L);

        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));
        when(receiptAnalysisItemRepository.findById(11L)).thenReturn(Optional.of(item));
        when(receiptAnalysisRepository.findByReceiptAnalysisIdAndMemberId(1L, 7L))
                .thenReturn(Optional.of(ReceiptAnalysis.createPending(7L, "receipt.jpeg", "receipts/7/test.jpeg")));
        when(productRepository.findById(20L)).thenReturn(Optional.of(tofu));

        ReceiptAnalysisItemResponse response = receiptService.updateReceiptAnalysisItem(
                "user@test.com",
                11L,
                new com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptAnalysisItemUpdateRequest(20L, "2개")
        );

        assertThat(response.productId()).isEqualTo(20L);
        assertThat(response.matchedProductName()).isEqualTo("두부");
        assertThat(response.quantity()).isEqualTo("2개");
        assertThat(response.expiryDate()).isEqualTo(LocalDate.now().plusDays(5));
    }

    @Test
    @DisplayName("PENDING OCR 후보를 REJECTED 상태로 제외한다")
    void rejectReceiptAnalysisItem_success() {
        Member member = Member.createUser("user@test.com", "encoded", "사용자", null);
        ReflectionTestUtils.setField(member, "id", 7L);

        ReceiptAnalysisItem item = ReceiptAnalysisItem.createPending(
                1L,
                10L,
                "국산 팽이버섯",
                "팽이버섯",
                "팽이버섯",
                "1개",
                LocalDate.now().plusDays(3)
        );

        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));
        when(receiptAnalysisItemRepository.findById(11L)).thenReturn(Optional.of(item));
        when(receiptAnalysisRepository.findByReceiptAnalysisIdAndMemberId(1L, 7L))
                .thenReturn(Optional.of(ReceiptAnalysis.createPending(7L, "receipt.jpeg", "receipts/7/test.jpeg")));

        receiptService.rejectReceiptAnalysisItem("user@test.com", 11L);

        assertThat(item.getStatus()).isEqualTo(ReceiptAnalysisItemStatus.REJECTED);
    }

    @Test
    @DisplayName("선택한 PENDING OCR 후보만 기존 냉장고 등록 서비스로 등록하고 REGISTERED 처리한다")
    void registerReceiptItemsToFridge_selectedItems() {
        Member member = Member.createUser("user@test.com", "encoded", "사용자", null);
        ReflectionTestUtils.setField(member, "id", 7L);

        ReceiptAnalysisItem item = ReceiptAnalysisItem.createPending(
                1L,
                10L,
                "국산 팽이버섯",
                "팽이버섯",
                "팽이버섯",
                "1개",
                LocalDate.now().plusDays(3)
        );
        ReflectionTestUtils.setField(item, "receiptAnalysisItemId", 11L);

        ReceiptAnalysisItem rejected = ReceiptAnalysisItem.createPending(
                1L,
                20L,
                "유상봉투",
                "유상봉투",
                "유상봉투",
                "1개",
                null
        );
        ReflectionTestUtils.setField(rejected, "receiptAnalysisItemId", 12L);
        rejected.reject();

        when(memberRepository.findByEmail("user@test.com")).thenReturn(Optional.of(member));
        when(receiptAnalysisRepository.findByReceiptAnalysisIdAndMemberId(1L, 7L))
                .thenReturn(Optional.of(ReceiptAnalysis.createPending(7L, "receipt.jpeg", "receipts/7/test.jpeg")));
        when(receiptAnalysisItemRepository.findByReceiptAnalysisIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(item, rejected));
        when(fridgeItemService.createFridgeItem(anyString(), any(FridgeItemCreateRequest.class)))
                .thenReturn(new FridgeItemResponse(100L, 10L, "1개", item.getExpiryDate(), "영수증으로 일괄 등록"));

        List<FridgeItemResponse> response = receiptService.registerReceiptItemsToFridge(
                "user@test.com",
                1L,
                new com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptFridgeRegisterRequest(List.of(11L))
        );

        assertThat(response).hasSize(1);
        assertThat(item.getStatus()).isEqualTo(ReceiptAnalysisItemStatus.REGISTERED);
        assertThat(rejected.getStatus()).isEqualTo(ReceiptAnalysisItemStatus.REJECTED);
        verify(fridgeItemService).createFridgeItem(anyString(), any(FridgeItemCreateRequest.class));
    }
}
