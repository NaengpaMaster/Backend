package com.naengpa.naengpamasterbackend.global.auth.service;

import com.naengpa.naengpamasterbackend.global.auth.entity.EmailVerification;
import com.naengpa.naengpamasterbackend.global.auth.repository.EmailVerificationRepository;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateEmailException;
import com.naengpa.naengpamasterbackend.global.exception.EmailVerificationException;
import com.naengpa.naengpamasterbackend.global.exception.WithdrawnEmailException;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int VERIFICATION_CODE_BOUND = 1_000_000;
    private static final int CODE_EXPIRE_MINUTES = 10;
    private static final int VERIFIED_SIGNUP_LIMIT_MINUTES = 30;

    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public void sendVerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        memberRepository.findByEmail(normalizedEmail)
                .ifPresent(member -> {
                    if (member.isInactive()) {
                        throw new WithdrawnEmailException();
                    }
                    throw new DuplicateEmailException();
                });

        String code = generateCode();
        EmailVerification emailVerification = EmailVerification.create(
                normalizedEmail,
                code,
                LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES)
        );
        emailVerificationRepository.save(emailVerification);
        sendMail(normalizedEmail, code);
    }


    @Transactional
    public void sendOAuth2VerificationCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        memberRepository.findByEmail(normalizedEmail)
                .ifPresent(member -> {
                    if (member.isInactive()) {
                        throw new WithdrawnEmailException();
                    }
                });

        String code = generateCode();
        EmailVerification emailVerification = EmailVerification.create(
                normalizedEmail,
                code,
                LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES)
        );
        emailVerificationRepository.save(emailVerification);
        sendMail(normalizedEmail, code);
    }

    @Transactional
    public void confirmVerificationCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        EmailVerification emailVerification = emailVerificationRepository.findFirstByEmailOrderByCreatedAtDesc(normalizedEmail)
                .orElseThrow(() -> new EmailVerificationException("인증 코드를 먼저 발송해주세요."));

        LocalDateTime now = LocalDateTime.now();
        if (emailVerification.isExpired(now)) {
            throw new EmailVerificationException("인증 코드가 만료되었습니다. 다시 발송해주세요.");
        }
        if (!emailVerification.canRetry()) {
            throw new EmailVerificationException("인증 코드 입력 횟수를 초과했습니다. 다시 발송해주세요.");
        }

        if (!emailVerification.matches(code)) {
            emailVerification.increaseAttemptCount();
            if (!emailVerification.canRetry()) {
                throw new EmailVerificationException("인증 코드 입력 횟수를 초과했습니다. 다시 발송해주세요.");
            }
            throw new EmailVerificationException("인증 코드가 일치하지 않습니다.");
        }

        emailVerification.verify(now);
    }

    @Transactional(readOnly = true)
    public void validateVerifiedEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        LocalDateTime validAfter = LocalDateTime.now().minusMinutes(VERIFIED_SIGNUP_LIMIT_MINUTES);
        boolean verified = emailVerificationRepository.existsByEmailAndVerifiedTrueAndVerifiedAtAfter(
                normalizedEmail,
                validAfter
        );

        if (!verified) {
            throw new EmailVerificationException("이메일 인증 후 회원가입해주세요.");
        }
    }

    private void sendMail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[냉파마스터] 이메일 인증 코드");
        message.setText("""
                냉파마스터 이메일 인증 코드입니다.

                인증 코드: %s

                인증 코드는 %d분 동안 유효합니다.
                """.formatted(code, CODE_EXPIRE_MINUTES));
        try {
            mailSender.send(message);
        } catch (RuntimeException exception) {
            throw new EmailVerificationException("이메일 발송에 실패했습니다. 메일 설정을 확인해주세요.");
        }
    }

    private String generateCode() {
        return "%06d".formatted(SECURE_RANDOM.nextInt(VERIFICATION_CODE_BOUND));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
