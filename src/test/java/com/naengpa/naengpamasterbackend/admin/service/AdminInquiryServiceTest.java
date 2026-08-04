package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminAnswerRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryDetailResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminInquiryResponse;
import com.naengpa.naengpamasterbackend.admin.repository.AdminInquiryAnswerRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminInquiryRepository;
import com.naengpa.naengpamasterbackend.global.exception.InquiryAlreadyAnsweredException;
import com.naengpa.naengpamasterbackend.global.exception.InquiryAnswerNotFoundException;
import com.naengpa.naengpamasterbackend.global.exception.InquiryNotFoundException;
import com.naengpa.naengpamasterbackend.inquiry.entity.Inquiry;
import com.naengpa.naengpamasterbackend.inquiry.entity.InquiryAnswer;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdminInquiryServiceTest {

    private AdminInquiryRepository adminInquiryRepository;
    private AdminInquiryAnswerRepository adminInquiryAnswerRepository;
    private MemberRepository memberRepository;
    private NotificationService notificationService;
    private AdminInquiryService adminInquiryService;

    @BeforeEach
    void setUp() {
        adminInquiryRepository = mock(AdminInquiryRepository.class);
        adminInquiryAnswerRepository = mock(AdminInquiryAnswerRepository.class);
        memberRepository = mock(MemberRepository.class);
        notificationService = mock(NotificationService.class);
        adminInquiryService = new AdminInquiryService(
                adminInquiryRepository,
                adminInquiryAnswerRepository,
                memberRepository,
                notificationService
        );
    }

    @Test
    void getInquiriesUsesCreatedAtAscendingByDefaultForPendingInquiries() {
        Pageable requestedPageable = PageRequest.of(2, 15, Sort.by("title"));
        Page<AdminInquiryResponse> expected = Page.empty();
        given(adminInquiryRepository.findInquiryList(any(), any(Pageable.class)))
                .willReturn(expected);

        Page<AdminInquiryResponse> result = adminInquiryService.getInquiries(
                false,
                null,
                requestedPageable
        );

        ArgumentCaptor<Boolean> answeredCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminInquiryRepository).findInquiryList(
                answeredCaptor.capture(),
                pageableCaptor.capture()
        );

        Pageable queryPageable = pageableCaptor.getValue();
        Sort.Order order = queryPageable.getSort().getOrderFor("createdAt");
        assertThat(result).isSameAs(expected);
        assertThat(answeredCaptor.getValue()).isFalse();
        assertThat(queryPageable.getPageNumber()).isEqualTo(2);
        assertThat(queryPageable.getPageSize()).isEqualTo(15);
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(queryPageable.getSort().getOrderFor("answeredAt")).isNull();
    }

    @Test
    void getInquiriesUsesAnsweredAtDescendingByDefaultForAnsweredInquiries() {
        Pageable requestedPageable = PageRequest.of(0, 10);
        given(adminInquiryRepository.findInquiryList(any(), any(Pageable.class)))
                .willReturn(Page.empty());

        adminInquiryService.getInquiries(true, null, requestedPageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminInquiryRepository).findInquiryList(any(), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("answeredAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNull();
    }

    @Test
    void getInquiriesUsesRequestedAscendingDirectionForAnsweredInquiries() {
        Pageable requestedPageable = PageRequest.of(1, 20);
        given(adminInquiryRepository.findInquiryList(any(), any(Pageable.class)))
                .willReturn(Page.empty());

        adminInquiryService.getInquiries(true, Sort.Direction.ASC, requestedPageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminInquiryRepository).findInquiryList(any(), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("answeredAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void getInquiriesUsesRequestedDescendingDirectionForPendingInquiries() {
        Pageable requestedPageable = PageRequest.of(0, 10);
        given(adminInquiryRepository.findInquiryList(any(), any(Pageable.class)))
                .willReturn(Page.empty());

        adminInquiryService.getInquiries(false, Sort.Direction.DESC, requestedPageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminInquiryRepository).findInquiryList(any(), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getInquiryDetailReturnsProjectedResponse() {
        Long inquiryId = 1L;
        AdminInquiryDetailResponse expected = new AdminInquiryDetailResponse(
                inquiryId,
                10L,
                "문의 제목",
                "문의 내용",
                "회원 닉네임",
                true,
                LocalDateTime.of(2026, 7, 31, 10, 0),
                2L,
                "답변 내용",
                20L,
                LocalDateTime.of(2026, 7, 31, 11, 0)
        );
        given(adminInquiryRepository.findInquiryDetail(inquiryId))
                .willReturn(Optional.of(expected));

        AdminInquiryDetailResponse result = adminInquiryService.getInquiryDetail(inquiryId);

        assertThat(result).isSameAs(expected);
        verify(adminInquiryRepository).findInquiryDetail(inquiryId);
        verify(adminInquiryAnswerRepository, never()).findByInquiryIdAndIsDeletedFalse(any());
        verify(memberRepository, never()).findById(any());
    }

    @Test
    void getInquiryDetailRejectsMissingInquiry() {
        Long inquiryId = 1L;
        given(adminInquiryRepository.findInquiryDetail(inquiryId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> adminInquiryService.getInquiryDetail(inquiryId))
                .isInstanceOf(InquiryNotFoundException.class);
    }

    @Test
    void createInquiryAnswerSavesAnswerAndUpdatesInquiry() {
        Long inquiryId = 1L;
        Long memberId = 10L;
        Long adminId = 20L;
        String adminEmail = "admin@example.com";
        Inquiry inquiry = mock(Inquiry.class);
        Member admin = mock(Member.class);
        AdminAnswerRequest request = new AdminAnswerRequest("답변 내용");

        given(inquiry.getId()).willReturn(inquiryId);
        given(inquiry.getMemberId()).willReturn(memberId);
        given(admin.getId()).willReturn(adminId);
        given(adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId))
                .willReturn(Optional.of(inquiry));
        given(adminInquiryAnswerRepository.existsByInquiryIdAndIsDeletedFalse(inquiryId))
                .willReturn(false);
        given(memberRepository.findByEmail(adminEmail)).willReturn(Optional.of(admin));

        adminInquiryService.createInquiryAnswer(inquiryId, request, adminEmail);

        ArgumentCaptor<InquiryAnswer> captor = ArgumentCaptor.forClass(InquiryAnswer.class);
        verify(adminInquiryAnswerRepository).saveAndFlush(captor.capture());
        InquiryAnswer savedAnswer = captor.getValue();
        assertThat(savedAnswer.getInquiryId()).isEqualTo(inquiryId);
        assertThat(savedAnswer.getContent()).isEqualTo(request.content());
        assertThat(savedAnswer.getCreatedBy()).isEqualTo(adminId);
        verify(inquiry).markAsAnswered();
        verify(notificationService).createInquiryAnsweredNotification(memberId, inquiryId);
    }

    @Test
    void createInquiryAnswerRejectsExistingActiveAnswer() {
        Long inquiryId = 1L;
        Inquiry inquiry = mock(Inquiry.class);

        given(adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId))
                .willReturn(Optional.of(inquiry));
        given(adminInquiryAnswerRepository.existsByInquiryIdAndIsDeletedFalse(inquiryId))
                .willReturn(true);

        assertThatThrownBy(() -> adminInquiryService.createInquiryAnswer(
                inquiryId,
                new AdminAnswerRequest("답변 내용"),
                "admin@example.com"
        )).isInstanceOf(InquiryAlreadyAnsweredException.class);

        verify(adminInquiryAnswerRepository, never()).saveAndFlush(any());
        verify(inquiry, never()).markAsAnswered();
        verify(notificationService, never()).createInquiryAnsweredNotification(any(), any());
    }

    @Test
    void createInquiryAnswerConvertsConcurrentDuplicateViolationToDomainException() {
        Long inquiryId = 1L;
        String adminEmail = "admin@example.com";
        Inquiry inquiry = mock(Inquiry.class);
        Member admin = mock(Member.class);

        given(admin.getId()).willReturn(20L);
        given(adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId))
                .willReturn(Optional.of(inquiry));
        given(adminInquiryAnswerRepository.existsByInquiryIdAndIsDeletedFalse(inquiryId))
                .willReturn(false);
        given(memberRepository.findByEmail(adminEmail)).willReturn(Optional.of(admin));
        ConstraintViolationException uniqueViolation = new ConstraintViolationException(
                "duplicate active answer",
                new SQLException("duplicate key"),
                "uq_inquiry_answers_active"
        );
        given(adminInquiryAnswerRepository.saveAndFlush(any(InquiryAnswer.class)))
                .willThrow(new DataIntegrityViolationException("duplicate active answer", uniqueViolation));

        assertThatThrownBy(() -> adminInquiryService.createInquiryAnswer(
                inquiryId,
                new AdminAnswerRequest("동시 등록 답변"),
                adminEmail
        )).isInstanceOf(InquiryAlreadyAnsweredException.class);

        verify(inquiry, never()).markAsAnswered();
        verify(notificationService, never()).createInquiryAnsweredNotification(any(), any());
    }

    @Test
    void updateInquiryAnswerUpdatesOnlyAnswerBelongingToInquiry() {
        Long inquiryId = 1L;
        Long answerId = 2L;
        Long adminId = 20L;
        String adminEmail = "admin@example.com";
        Inquiry inquiry = mock(Inquiry.class);
        InquiryAnswer answer = mock(InquiryAnswer.class);
        Member admin = mock(Member.class);
        AdminAnswerRequest request = new AdminAnswerRequest("수정된 답변");

        given(admin.getId()).willReturn(adminId);
        given(adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId))
                .willReturn(Optional.of(inquiry));
        given(adminInquiryAnswerRepository.findByIdAndInquiryIdAndIsDeletedFalse(answerId, inquiryId))
                .willReturn(Optional.of(answer));
        given(memberRepository.findByEmail(adminEmail)).willReturn(Optional.of(admin));

        adminInquiryService.updateInquiryAnswer(inquiryId, answerId, request, adminEmail);

        verify(answer).update(request.content(), adminId);
    }

    @Test
    void updateInquiryAnswerRejectsAnswerBelongingToAnotherInquiry() {
        Long inquiryId = 1L;
        Long answerId = 2L;

        given(adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId))
                .willReturn(Optional.of(mock(Inquiry.class)));
        given(adminInquiryAnswerRepository.findByIdAndInquiryIdAndIsDeletedFalse(answerId, inquiryId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> adminInquiryService.updateInquiryAnswer(
                inquiryId,
                answerId,
                new AdminAnswerRequest("수정된 답변"),
                "admin@example.com"
        )).isInstanceOf(InquiryAnswerNotFoundException.class);

        verify(memberRepository, never()).findByEmail(any());
    }

    @Test
    void deleteInquiryAnswerSoftDeletesAnswerAndMarksInquiryUnanswered() {
        Long inquiryId = 1L;
        Long answerId = 2L;
        Inquiry inquiry = mock(Inquiry.class);
        InquiryAnswer answer = mock(InquiryAnswer.class);

        given(adminInquiryRepository.findByIdAndIsDeletedFalse(inquiryId))
                .willReturn(Optional.of(inquiry));
        given(adminInquiryAnswerRepository.findByIdAndInquiryIdAndIsDeletedFalse(answerId, inquiryId))
                .willReturn(Optional.of(answer));

        adminInquiryService.deleteInquiryAnswer(inquiryId, answerId);

        verify(answer).delete();
        verify(inquiry).markAsUnanswered();
    }
}
