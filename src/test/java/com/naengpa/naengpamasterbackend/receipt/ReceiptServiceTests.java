package com.naengpa.naengpamasterbackend.receipt;

import com.naengpa.naengpamasterbackend.global.exception.InvalidReceiptImageException;
import com.naengpa.naengpamasterbackend.global.s3.S3Uploader;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.receipt.dto.response.ReceiptImageUploadResponse;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysis;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisStatus;
import com.naengpa.naengpamasterbackend.receipt.repository.ReceiptAnalysisRepository;
import com.naengpa.naengpamasterbackend.receipt.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

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
    private S3Uploader s3Uploader;
    private ReceiptService receiptService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        receiptAnalysisRepository = mock(ReceiptAnalysisRepository.class);
        s3Uploader = mock(S3Uploader.class);
        receiptService = new ReceiptService(memberRepository, receiptAnalysisRepository, s3Uploader);
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
}
