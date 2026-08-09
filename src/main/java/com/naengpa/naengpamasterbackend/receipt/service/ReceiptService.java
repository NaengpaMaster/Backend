package com.naengpa.naengpamasterbackend.receipt.service;

import com.naengpa.naengpamasterbackend.global.exception.InvalidReceiptImageException;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.global.s3.S3Uploader;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.receipt.dto.response.ReceiptImageUploadResponse;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysis;
import com.naengpa.naengpamasterbackend.receipt.repository.ReceiptAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ReceiptService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final MemberRepository memberRepository;
    private final ReceiptAnalysisRepository receiptAnalysisRepository;
    private final S3Uploader s3Uploader;

    public ReceiptService(
            MemberRepository memberRepository,
            ReceiptAnalysisRepository receiptAnalysisRepository,
            S3Uploader s3Uploader
    ) {
        this.memberRepository = memberRepository;
        this.receiptAnalysisRepository = receiptAnalysisRepository;
        this.s3Uploader = s3Uploader;
    }

    @Transactional
    public ReceiptImageUploadResponse uploadReceiptImage(String email, MultipartFile file) {
        Member member = findMemberByEmail(email);
        validateImage(file);

        // 원본 확장자를 유지한 S3 저장 경로를 만든 뒤, 이미지를 S3에 임시 업로드
        String extension = extractExtension(file);
        String objectKey = createObjectKey(member.getId(), extension);
        String uploadedObjectKey = s3Uploader.upload(file, objectKey);

        // OCR 분석 전 단계이므로 PENDING 상태의 분석 row를 생성
        ReceiptAnalysis receiptAnalysis = ReceiptAnalysis.createPending(
                member.getId(),
                file.getOriginalFilename(),
                uploadedObjectKey
        );

        ReceiptAnalysis saved = receiptAnalysisRepository.save(receiptAnalysis);
        return ReceiptImageUploadResponse.from(saved);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }

    // 업로드 가능한 파일인지 확인. 현재 정책은 10MB 이하의 jpg, jpeg, png만 허용
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidReceiptImageException("영수증 이미지를 업로드해주세요.");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidReceiptImageException("영수증 이미지는 10MB 이하만 업로드할 수 있습니다.");
        }

        String extension = extractExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidReceiptImageException("영수증 이미지는 jpg, jpeg, png 파일만 업로드할 수 있습니다.");
        }
    }

    // 파일명에서 확장자만 추출. JPG처럼 대문자로 들어와도 비교 가능하도록 소문자로 변경
    private String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null) {
            return "";
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    // 회원별 폴더 아래에 UUID 파일명으로 저장해 파일명 충돌과 원본 파일명 노출을 방지
    private String createObjectKey(Long memberId, String extension) {
        return "receipts/%d/%s.%s".formatted(
                memberId,
                UUID.randomUUID(),
                extension
        );
    }
}
