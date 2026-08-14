package com.naengpa.naengpamasterbackend.global.auth.service;

import com.naengpa.naengpamasterbackend.global.auth.dto.LoginRequest;
import com.naengpa.naengpamasterbackend.global.auth.dto.MemberResponse;
import com.naengpa.naengpamasterbackend.global.auth.dto.ProfileProductResponse;
import com.naengpa.naengpamasterbackend.global.auth.dto.ProfileUpdateRequest;
import com.naengpa.naengpamasterbackend.global.auth.dto.SignUpRequest;
import com.naengpa.naengpamasterbackend.global.auth.dto.TokenResponse;
import com.naengpa.naengpamasterbackend.global.auth.entity.RefreshToken;
import com.naengpa.naengpamasterbackend.global.auth.repository.RefreshTokenRepository;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateEmailException;
import com.naengpa.naengpamasterbackend.global.exception.DuplicateNicknameException;
import com.naengpa.naengpamasterbackend.global.exception.FoodCategoryNotFoundException;
import com.naengpa.naengpamasterbackend.global.exception.NicknameGenerationFailedException;
import com.naengpa.naengpamasterbackend.global.exception.PasswordMismatchException;
import com.naengpa.naengpamasterbackend.global.exception.WithdrawnEmailException;
import com.naengpa.naengpamasterbackend.global.security.JwtTokenProvider;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.member.entity.FoodCategory;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import com.naengpa.naengpamasterbackend.member.entity.MemberExcludedProduct;
import com.naengpa.naengpamasterbackend.member.entity.MemberFavoriteFood;
import com.naengpa.naengpamasterbackend.member.repository.FoodCategoryRepository;
import com.naengpa.naengpamasterbackend.member.repository.MemberExcludedProductRepository;
import com.naengpa.naengpamasterbackend.member.repository.MemberFavoriteFoodRepository;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.score.entity.Score;
import com.naengpa.naengpamasterbackend.score.repository.ScoreRepository;
import com.naengpa.naengpamasterbackend.subscription.dto.response.SubscriptionStatusResponse;
import com.naengpa.naengpamasterbackend.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int NANOS_PER_MILLISECOND = 1_000_000;
    private static final int MAX_NICKNAME_GENERATE_ATTEMPTS = 20;
    private static final Collator KOREAN_COLLATOR = Collator.getInstance(Locale.KOREAN);
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣A-Za-z0-9 ]+$");
    private static final String INVALID_NICKNAME_MESSAGE = "닉네임은 한글, 영문, 숫자, 공백만 사용할 수 있습니다.";

    private final MemberRepository memberRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final MemberFavoriteFoodRepository memberFavoriteFoodRepository;
    private final MemberExcludedProductRepository memberExcludedProductRepository;
    private final ProductRepository productRepository;
    private final ScoreRepository scoreRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SubscriptionService subscriptionService;
    private final EmailVerificationService emailVerificationService;
    private final FridgeService fridgeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MemberResponse signup(SignUpRequest request) {
        if (!request.password().equals(request.passwordConfirm())) {
            throw new PasswordMismatchException();
        }

        memberRepository.findByEmail(request.email())
                .ifPresent(member -> {
                    if (member.isInactive()) {
                        throw new WithdrawnEmailException();
                    }
                    throw new DuplicateEmailException();
                });
        emailVerificationService.validateVerifiedEmail(request.email());

        Member member = Member.createUser(
                request.email(),
                passwordEncoder.encode(request.password()),
                resolveNickname(request.nickname()),
                request.householdType()
        );
        Member savedMember = memberRepository.save(member);
        fridgeService.createDefaultFridge(savedMember);
        scoreRepository.save(Score.createInitial(savedMember.getId()));
        return MemberResponse.from(savedMember);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("올바르지 않은 이메일 또는 비밀번호입니다."));

        if (member.isInactive()) {
            throw new DisabledException("탈퇴 처리된 회원입니다. 관리자에게 문의해주세요.");
        }

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BadCredentialsException("올바르지 않은 이메일 또는 비밀번호입니다.");
        }

        return issueTokens(member);
    }

    @Transactional
    public TokenResponse refreshToken(String providedRefreshToken) {
        jwtTokenProvider.validateToken(providedRefreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByRefreshToken(providedRefreshToken)
                .orElseThrow(() -> new BadCredentialsException("저장된 리프레시 토큰이 없습니다."));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new BadCredentialsException("리프레시 토큰이 만료되었습니다.");
        }

        Member member = storedToken.getMember();

        if (member.isInactive()) {
            storedToken.expireNow();
            throw new DisabledException("탈퇴 처리된 회원입니다. 관리자에게 문의해주세요.");
        }

        return issueAccessToken(member, storedToken.getRefreshToken());
    }

    @Transactional
    public void logout(String refreshToken) {
        jwtTokenProvider.validateToken(refreshToken);
        refreshTokenRepository.findByRefreshToken(refreshToken)
                .ifPresent(RefreshToken::expireNow);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));
        return toMemberResponse(member);
    }

    @Transactional
    public MemberResponse updateProfile(String email, ProfileUpdateRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));

        updateNickname(member, request.nickname());
        member.updateHouseholdType(request.householdType());
        replaceFavoriteFoods(member, request.favoriteFoods());
        replaceExcludedProducts(member, request.avoidProductIds());

        return toMemberResponse(member);
    }

    @Transactional
    public void withdraw(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("회원을 찾을 수 없습니다."));

        SubscriptionStatusResponse subscriptionStatus = subscriptionService.getMySubscription(email);
        if (subscriptionStatus.premium() && !subscriptionStatus.cancelReserved()) {
            throw new IllegalStateException("현재 구독 중이라 회원탈퇴가 불가합니다. 구독 취소 후 다시 시도해주세요.");
        }

        member.updateStatus(MemberStatus.INACTIVE);
        expireActiveRefreshTokens(member);
    }

    private TokenResponse issueTokens(Member member) {
        expireActiveRefreshTokens(member);

        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().name());

        RefreshToken tokenEntity = RefreshToken.builder()
                .member(member)
                .refreshToken(refreshToken)
                .expiredAt(LocalDateTime.now()
                        .plusNanos(jwtTokenProvider.getRefreshExpiration() * NANOS_PER_MILLISECOND))
                .build();
        refreshTokenRepository.save(tokenEntity);

        return new TokenResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                accessToken,
                refreshToken
        );
    }

    private TokenResponse issueAccessToken(Member member, String refreshToken) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());

        return new TokenResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getRole(),
                accessToken,
                refreshToken
        );
    }

    private void expireActiveRefreshTokens(Member member) {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.findAllByMemberAndExpiredAtAfter(member, now)
                .forEach(RefreshToken::expireNow);
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

    private MemberResponse toMemberResponse(Member member) {
        List<String> favoriteFoods = memberFavoriteFoodRepository.findAllByMemberOrderByIdAsc(member)
                .stream()
                .map(memberFavoriteFood -> memberFavoriteFood.getFoodCategory().getName())
                .toList();
        List<ProfileProductResponse> avoidIngredients = memberExcludedProductRepository.findAllByMemberWithProduct(member)
                .stream()
                .map(MemberExcludedProduct::getProduct)
                .sorted(Comparator.comparing(Product::getName, KOREAN_COLLATOR))
                .map(ProfileProductResponse::from)
                .toList();

        return MemberResponse.from(member, favoriteFoods, avoidIngredients);
    }

    private void updateNickname(Member member, String nickname) {
        String trimmedNickname = nickname.trim();
        validateNickname(trimmedNickname);
        if (member.getNickname().equals(trimmedNickname)) {
            return;
        }
        if (memberRepository.existsByNickname(trimmedNickname)) {
            throw new DuplicateNicknameException();
        }
        member.updateNickname(trimmedNickname);
    }

    private void validateNickname(String nickname) {
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new IllegalArgumentException(INVALID_NICKNAME_MESSAGE);
        }
    }

    private void replaceFavoriteFoods(Member member, List<String> favoriteFoods) {
        memberFavoriteFoodRepository.deleteAllByMember(member);
        memberFavoriteFoodRepository.flush();

        uniqueFoodCategories(favoriteFoods).stream()
                .map(foodCategory -> MemberFavoriteFood.create(member, foodCategory))
                .forEach(memberFavoriteFoodRepository::save);
    }

    private void replaceExcludedProducts(Member member, List<Long> productIds) {
        memberExcludedProductRepository.deleteAllByMember(member);
        memberExcludedProductRepository.flush();

        List<Long> uniqueProductIds = uniqueLongs(productIds);
        if (uniqueProductIds.isEmpty()) {
            return;
        }

        List<Product> products = productRepository.findByProductIdInAndIsActiveTrue(uniqueProductIds);
        if (products.size() != uniqueProductIds.size()) {
            throw new ProductNotFoundException(null);
        }

        products.stream()
                .map(product -> MemberExcludedProduct.create(member, product))
                .forEach(memberExcludedProductRepository::save);
    }

    private List<String> uniqueTrimmedStrings(List<String> values) {
        if (values == null) {
            return List.of();
        }

        Set<String> uniqueValues = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            uniqueValues.add(value.trim());
        }
        return List.copyOf(uniqueValues);
    }

    private List<FoodCategory> uniqueFoodCategories(List<String> foodNames) {
        List<String> uniqueFoodNames = uniqueTrimmedStrings(foodNames);
        if (uniqueFoodNames.isEmpty()) {
            return List.of();
        }

        List<FoodCategory> savedCategories = foodCategoryRepository.findByNameIn(uniqueFoodNames);
        if (savedCategories.size() != uniqueFoodNames.size()) {
            throw new FoodCategoryNotFoundException();
        }

        Map<String, FoodCategory> categoryByName = savedCategories.stream()
                .collect(Collectors.toMap(FoodCategory::getName, category -> category));

        return uniqueFoodNames.stream()
                .map(categoryByName::get)
                .toList();
    }

    private List<Long> uniqueLongs(List<Long> values) {
        if (values == null) {
            return List.of();
        }

        Set<Long> uniqueValues = new LinkedHashSet<>();
        for (Long value : values) {
            if (value != null) {
                uniqueValues.add(value);
            }
        }
        return List.copyOf(uniqueValues);
    }
}
