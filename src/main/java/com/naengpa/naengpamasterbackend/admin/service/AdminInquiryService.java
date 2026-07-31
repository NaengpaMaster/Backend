package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminAnswerRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryDetailResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryResponse;
import com.naengpa.naengpamasterbackend.admin.repository.AdminInquiryAnswerRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminInquiryRepository;
import com.naengpa.naengpamasterbackend.global.exception.InquiryAlreadyAnsweredException;
import com.naengpa.naengpamasterbackend.global.exception.InquiryAnswerNotFoundException;
import com.naengpa.naengpamasterbackend.global.exception.InquiryNotFoundException;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.inquiry.entity.Inquiry;
import com.naengpa.naengpamasterbackend.inquiry.entity.InquiryAnswer;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminInquiryService {

    private static final String ACTIVE_ANSWER_UNIQUE_CONSTRAINT = "uq_inquiry_answers_active";
    private final AdminInquiryRepository adminInquiryRepository;
    private final AdminInquiryAnswerRepository adminInquiryAnswerRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<AdminInquiryResponse> getInquiries(Boolean isAnswered, Pageable pageable) {
        if (isAnswered) {
            return adminInquiryRepository
                    .findByIsAnsweredAndIsDeletedFalseOrderByAnsweredAtDesc(isAnswered, pageable)
                    .map(inquiry -> {
                        Member member = memberRepository.findById(inquiry.getMemberId()).orElse(null);
                        String nickname = member != null ? member.getNickname() : null;
                        return AdminInquiryResponse.from(inquiry, nickname);
                    });
        }

        return adminInquiryRepository.findByIsAnsweredAndIsDeletedFalseOrderByCreatedAtAsc(isAnswered, pageable)
                .map(inquiry -> {
                    Member member = memberRepository.findById(inquiry.getMemberId()).orElse(null);
                    String nickname = member != null ? member.getNickname() : null;
                    return AdminInquiryResponse.from(inquiry, nickname);
                });
    }

    @Transactional(readOnly = true)
    public AdminInquiryDetailResponse getInquiryDetail(Long inquiryId) {
        Inquiry inquiry = adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(InquiryNotFoundException::new);

        InquiryAnswer inquiryAnswer = adminInquiryAnswerRepository
                .findByInquiryIdAndIsDeletedFalse(inquiryId).orElse(null);

        Member member = memberRepository.findById(inquiry.getMemberId()).orElse(null);
        String nickname = member != null ? member.getNickname() : null;
        return AdminInquiryDetailResponse.from(inquiry, inquiryAnswer, nickname);
    }

    /**
     * 관리자 문의 답변을 등록하며, 사전 검사와 DB UNIQUE 제약으로 중복 답변을 방지한다.
     */
    @Transactional
    public void createInquiryAnswer(Long inquiryId, AdminAnswerRequest request, String adminEmail) {
        Inquiry inquiry = adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(InquiryNotFoundException::new);

        boolean existInquiryAnswer = adminInquiryAnswerRepository
                .existsByInquiryIdAndIsDeletedFalse(inquiryId);

        // 답변이 이미 존재하면 예외 발생
        if (existInquiryAnswer) {
            throw new InquiryAlreadyAnsweredException();
        }

        Long adminId = resolveAdminIdOrThrow(adminEmail);

        InquiryAnswer inquiryAnswer = InquiryAnswer.create(inquiryId, request.content(), adminId);

        // 답변 등록 동시 요청으로 발생하는 중복 답변 저장을 DB UNIQUE 제약으로 방지함
        try {
            adminInquiryAnswerRepository.saveAndFlush(inquiryAnswer);
        } catch (DataIntegrityViolationException exception) {
            if (isActiveAnswerUniqueViolation(exception)) {
                throw new InquiryAlreadyAnsweredException();
            }
            throw exception;
        }

        inquiry.markAsAnswered();
        notificationService.createInquiryAnsweredNotification(inquiry.getMemberId(), inquiry.getId());
    }

    @Transactional
    public void updateInquiryAnswer(Long inquiryId, Long answerId, AdminAnswerRequest request, String adminEmail) {
        adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(InquiryNotFoundException::new);

        InquiryAnswer inquiryAnswer = adminInquiryAnswerRepository
                .findByIdAndInquiryIdAndIsDeletedFalse(answerId, inquiryId)
                .orElseThrow(InquiryAnswerNotFoundException::new);

        Long adminId = resolveAdminIdOrThrow(adminEmail);

        inquiryAnswer.update(request.content(), adminId);
    }

    @Transactional
    public void deleteInquiryAnswer(Long inquiryId, Long answerId) {
        Inquiry inquiry = adminInquiryRepository
                .findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(InquiryNotFoundException::new);

        InquiryAnswer inquiryAnswer = adminInquiryAnswerRepository
                .findByIdAndInquiryIdAndIsDeletedFalse(answerId, inquiryId)
                .orElseThrow(InquiryAnswerNotFoundException::new);

        inquiryAnswer.delete();
        inquiry.markAsUnanswered();
    }

    @Transactional
    public void deleteInquiry(Long inquiryId) {
        Inquiry inquiry = adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId)
                .orElseThrow(InquiryNotFoundException::new);

        adminInquiryAnswerRepository
                .findByInquiryIdAndIsDeletedFalse(inquiryId)
                .ifPresent(InquiryAnswer::delete);

        inquiry.delete();
    }

    private Long resolveAdminIdOrThrow(String email) {
        if (!StringUtils.hasText(email)) {
            throw new MemberNotFoundException();
        }
        return memberRepository.findByEmail(email)
                .map(Member::getId)
                .orElseThrow(MemberNotFoundException::new);
    }

    /**
     * 예외 원인을 순회하며 활성 답변 중복 방지 UNIQUE 제약조건 위반인지 확인함
     */
    private boolean isActiveAnswerUniqueViolation(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation) {
                return ACTIVE_ANSWER_UNIQUE_CONSTRAINT.equals(
                        constraintViolation.getConstraintName()
                );
            }

            cause = cause.getCause();
        }

        return false;
    }
}
