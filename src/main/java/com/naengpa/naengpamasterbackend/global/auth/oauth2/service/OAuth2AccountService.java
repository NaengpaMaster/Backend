package com.naengpa.naengpamasterbackend.global.auth.oauth2.service;

import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.global.auth.dto.TokenResponse;
import com.naengpa.naengpamasterbackend.global.auth.entity.RefreshToken;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.dto.OAuth2SignupRequiredResponse;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.dto.SocialAccountResponse;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2Provider;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2SignupToken;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.SocialAccount;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.repository.OAuth2SignupTokenRepository;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.repository.SocialAccountRepository;
import com.naengpa.naengpamasterbackend.global.auth.repository.RefreshTokenRepository;
import com.naengpa.naengpamasterbackend.global.auth.service.EmailVerificationService;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateEmailException;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateNicknameException;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.global.exception.NicknameGenerationFailedException;
import com.naengpa.naengpamasterbackend.global.exception.WithdrawnEmailException;
import com.naengpa.naengpamasterbackend.global.security.JwtTokenProvider;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.score.entity.Score;
import com.naengpa.naengpamasterbackend.score.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OAuth2AccountService {

    private static final int NANOS_PER_MILLISECOND = 1_000_000;
    private static final int SIGNUP_TOKEN_BYTES = 32;
    private static final int SIGNUP_TOKEN_EXPIRE_MINUTES = 15;
    private static final int MAX_NICKNAME_GENERATE_ATTEMPTS = 20;
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣A-Za-z0-9 ]+$");
    private static final String INVALID_NICKNAME_MESSAGE = "닉네임은 한글, 영문, 숫자, 공백만 사용할 수 있습니다.";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SocialAccountRepository socialAccountRepository;
    private final OAuth2SignupTokenRepository oauth2SignupTokenRepository;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final FridgeService fridgeService;
    private final ScoreRepository scoreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    @Transactional(readOnly = true)
    public List<SocialAccountResponse> getLinkedAccounts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        return socialAccountRepository.findAllByMemberIdOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(SocialAccountResponse::from)
                .toList();
    }

    @Transactional
    public void unlinkAccount(String email, OAuth2Provider provider) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        if (socialAccountRepository.countByMemberId(member.getId()) <= 1) {
            throw new IllegalArgumentException("마지막 소셜 연동은 해지할 수 없습니다. 다른 로그인 수단을 먼저 연결해주세요.");
        }

        SocialAccount socialAccount = socialAccountRepository.findByMemberIdAndProvider(member.getId(), provider)
                .orElseThrow(() -> new IllegalArgumentException("연동된 소셜 계정이 없습니다."));
        socialAccountRepository.delete(socialAccount);
    }

    @Transactional
    public TokenResponse linkAccount(String accessToken, OAuth2Provider provider, String providerUserId, String providerEmail) {
        validateProviderUserId(providerUserId);
        if (accessToken == null || accessToken.isBlank()) {
            throw new BadCredentialsException("소셜 연동을 위해 로그인이 필요합니다.");
        }
        jwtTokenProvider.validateToken(accessToken);

        String email = jwtTokenProvider.getEmail(accessToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .ifPresent(existingAccount -> {
                    if (!existingAccount.getMemberId().equals(member.getId())) {
                        throw new IllegalArgumentException("이미 다른 회원에게 연동된 소셜 계정입니다.");
                    }
                    throw new IllegalArgumentException("이미 연동된 소셜 계정입니다.");
                });

        socialAccountRepository.findByMemberIdAndProvider(member.getId(), provider)
                .ifPresent(existingAccount -> {
                    throw new IllegalArgumentException("이미 같은 제공자의 소셜 계정이 연동되어 있습니다.");
                });

        socialAccountRepository.save(SocialAccount.create(member.getId(), provider, providerUserId, providerEmail));
        return issueTokens(member);
    }

    @Transactional
    public Optional<TokenResponse> loginLinkedAccount(OAuth2Provider provider, String providerUserId) {
        validateProviderUserId(providerUserId);
        return socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(SocialAccount::getMemberId)
                .flatMap(memberRepository::findById)
                .map(this::issueTokens);
    }

    @Transactional
    public TokenResponse loginOrCreateWithVerifiedEmail(
            OAuth2Provider provider,
            String providerUserId,
            String providerEmail
    ) {
        validateProviderUserId(providerUserId);
        String normalizedEmail = normalizeRequiredEmail(providerEmail);

        return socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(SocialAccount::getMemberId)
                .flatMap(memberRepository::findById)
                .map(this::issueTokens)
                .orElseGet(() -> connectOrCreateMember(
                        provider,
                        providerUserId,
                        normalizedEmail,
                        normalizedEmail,
                        null,
                        HouseholdType.ETC
                ));
    }

    @Transactional
    public OAuth2SignupRequiredResponse createEmailRequiredToken(
            OAuth2Provider provider,
            String providerUserId,
            String providerEmail
    ) {
        validateProviderUserId(providerUserId);
        String token = generateSignupToken();
        OAuth2SignupToken savedToken = oauth2SignupTokenRepository.save(OAuth2SignupToken.create(
                provider,
                providerUserId,
                normalizeEmail(providerEmail),
                token,
                LocalDateTime.now().plusMinutes(SIGNUP_TOKEN_EXPIRE_MINUTES)
        ));

        return new OAuth2SignupRequiredResponse(
                true,
                savedToken.getToken(),
                savedToken.getProvider().name(),
                savedToken.getProviderEmail()
        );
    }

    @Transactional
    public TokenResponse completeEmail(String signupToken, String email, String nickname, HouseholdType householdType) {
        OAuth2SignupToken token = oauth2SignupTokenRepository.findByToken(signupToken)
                .orElseThrow(() -> new BadCredentialsException("유효하지 않은 OAuth2 가입 토큰입니다."));

        LocalDateTime now = LocalDateTime.now();
        if (token.isCompleted()) {
            throw new BadCredentialsException("이미 사용된 OAuth2 가입 토큰입니다.");
        }
        if (token.isExpired(now)) {
            throw new BadCredentialsException("OAuth2 가입 토큰이 만료되었습니다. 다시 로그인해주세요.");
        }

        String normalizedEmail = normalizeRequiredEmail(email);
        emailVerificationService.validateVerifiedEmail(normalizedEmail);

        TokenResponse tokenResponse = connectOrCreateMember(
                token.getProvider(),
                token.getProviderUserId(),
                token.getProviderEmail(),
                normalizedEmail,
                nickname,
                householdType
        );
        token.complete();
        return tokenResponse;
    }

    private TokenResponse connectOrCreateMember(
            OAuth2Provider provider,
            String providerUserId,
            String providerEmail,
            String serviceEmail,
            String nickname,
            HouseholdType householdType
    ) {
        if (socialAccountRepository.existsByProviderAndProviderUserId(provider, providerUserId)) {
            throw new BadCredentialsException("이미 연결된 소셜 계정입니다.");
        }

        Member member = memberRepository.findByEmail(serviceEmail)
                .map(existingMember -> {
                    if (existingMember.isInactive()) {
                        throw new WithdrawnEmailException();
                    }
                    return existingMember;
                })
                .orElseGet(() -> createSocialMember(serviceEmail, nickname, householdType));

        socialAccountRepository.save(SocialAccount.create(member.getId(), provider, providerUserId, providerEmail));
        return issueTokens(member);
    }

    private Member createSocialMember(String email, String nickname, HouseholdType householdType) {
        memberRepository.findByEmail(email)
                .ifPresent(member -> {
                    if (member.isInactive()) {
                        throw new WithdrawnEmailException();
                    }
                    throw new DuplicateEmailException();
                });

        Member member = Member.createUser(
                email,
                passwordEncoder.encode(generateSignupToken()),
                resolveNickname(nickname),
                householdType
        );
        Member savedMember = memberRepository.save(member);
        fridgeService.createDefaultFridge(savedMember);
        scoreRepository.save(Score.createInitial(savedMember.getId()));
        return savedMember;
    }

    private TokenResponse issueTokens(Member member) {
        if (member.isInactive()) {
            throw new DisabledException("탈퇴 처리된 회원입니다. 관리자에게 문의해주세요.");
        }
        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new BadCredentialsException("이메일 인증 후 소셜 로그인을 완료해주세요.");
        }

        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.findAllByMemberAndExpiredAtAfter(member, now)
                .forEach(RefreshToken::expireNow);

        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().name());
        refreshTokenRepository.save(RefreshToken.builder()
                .member(member)
                .refreshToken(refreshToken)
                .expiredAt(now.plusNanos(jwtTokenProvider.getRefreshExpiration() * NANOS_PER_MILLISECOND))
                .build());

        return new TokenResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                accessToken,
                refreshToken
        );
    }

    private String resolveNickname(String nickname) {
        if (nickname != null && !nickname.isBlank()) {
            String trimmedNickname = nickname.trim();
            validateNickname(trimmedNickname);
            if (memberRepository.existsByNickname(trimmedNickname)) {
                throw new DuplicateNicknameException();
            }
            return trimmedNickname;
        }

        for (int attempt = 0; attempt < MAX_NICKNAME_GENERATE_ATTEMPTS; attempt++) {
            String generatedNickname = Member.generateRandomNickname();
            if (!memberRepository.existsByNickname(generatedNickname)) {
                return generatedNickname;
            }
        }
        throw new NicknameGenerationFailedException();
    }

    private void validateNickname(String nickname) {
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new IllegalArgumentException(INVALID_NICKNAME_MESSAGE);
        }
    }

    private void validateProviderUserId(String providerUserId) {
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("provider 사용자 ID는 필수입니다.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("소셜 로그인 완료를 위해 이메일은 필수입니다.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateSignupToken() {
        byte[] bytes = new byte[SIGNUP_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
