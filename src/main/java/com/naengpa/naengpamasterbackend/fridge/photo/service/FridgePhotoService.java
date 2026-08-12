package com.naengpa.naengpamasterbackend.fridge.photo.service;

import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentLlmUsageResponse;
import com.naengpa.naengpamasterbackend.agent.usage.service.LlmUsageLogService;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemCreateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.request.FridgePhotoItemUpdateRequest;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.request.FridgePhotoItemsRegisterRequest;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.request.FridgePhotoOcrItemRequest;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.request.FridgePhotoOcrSaveRequest;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.response.FridgePhotoImageUploadResponse;
import com.naengpa.naengpamasterbackend.fridge.photo.dto.response.FridgePhotoItemResponse;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeItemService;
import com.naengpa.naengpamasterbackend.global.exception.InvalidReceiptImageException;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.global.s3.S3Uploader;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysis;
import com.naengpa.naengpamasterbackend.receipt.entity.ReceiptAnalysisItem;
import com.naengpa.naengpamasterbackend.receipt.repository.ReceiptAnalysisItemRepository;
import com.naengpa.naengpamasterbackend.receipt.repository.ReceiptAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FridgePhotoService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Map<String, String> PRODUCT_ALIASES = createProductAliases();

    private final MemberRepository memberRepository;
    private final ReceiptAnalysisRepository receiptAnalysisRepository;
    private final ReceiptAnalysisItemRepository receiptAnalysisItemRepository;
    private final ProductRepository productRepository;
    private final FridgeItemService fridgeItemService;
    private final S3Uploader s3Uploader;
    private final LlmUsageLogService llmUsageLogService;

    public FridgePhotoService(
            MemberRepository memberRepository,
            ReceiptAnalysisRepository receiptAnalysisRepository,
            ReceiptAnalysisItemRepository receiptAnalysisItemRepository,
            ProductRepository productRepository,
            FridgeItemService fridgeItemService,
            S3Uploader s3Uploader,
            LlmUsageLogService llmUsageLogService
    ) {
        this.memberRepository = memberRepository;
        this.receiptAnalysisRepository = receiptAnalysisRepository;
        this.receiptAnalysisItemRepository = receiptAnalysisItemRepository;
        this.productRepository = productRepository;
        this.fridgeItemService = fridgeItemService;
        this.s3Uploader = s3Uploader;
        this.llmUsageLogService = llmUsageLogService;
    }

    @Transactional
    public FridgePhotoImageUploadResponse uploadFridgePhoto(String email, MultipartFile file) {
        Member member = findMemberByEmail(email);
        validateImage(file);

        String extension = extractExtension(file);
        String objectKey = createObjectKey(member.getId(), extension);
        String uploadedObjectKey = s3Uploader.upload(file, objectKey);

        ReceiptAnalysis analysis = ReceiptAnalysis.createPending(
                member.getId(),
                file.getOriginalFilename(),
                uploadedObjectKey
        );

        return FridgePhotoImageUploadResponse.from(receiptAnalysisRepository.save(analysis));
    }

    @Transactional(readOnly = true)
    public List<FridgePhotoItemResponse> getItems(String email, Long fridgePhotoAnalysisId) {
        Member member = findMemberByEmail(email);
        receiptAnalysisRepository.findByReceiptAnalysisIdAndMemberId(fridgePhotoAnalysisId, member.getId())
                .orElseThrow();

        return receiptAnalysisItemRepository
                .findByReceiptAnalysisIdOrderByCreatedAtAsc(fridgePhotoAnalysisId)
                .stream()
                .map(FridgePhotoItemResponse::from)
                .toList();
    }

    @Transactional
    public List<FridgePhotoItemResponse> saveAnalysisResult(
            String email,
            Long fridgePhotoAnalysisId,
            FridgePhotoOcrSaveRequest request
    ) {
        Member member = findMemberByEmail(email);
        ReceiptAnalysis analysis = receiptAnalysisRepository
                .findByReceiptAnalysisIdAndMemberId(fridgePhotoAnalysisId, member.getId())
                .orElseThrow();

        analysis.updateRawOcrText(request.rawText());

        List<ReceiptAnalysisItem> items = request.items().stream()
                .map(item -> createMatchedItem(fridgePhotoAnalysisId, item))
                .flatMap(Optional::stream)
                .toList();

        receiptAnalysisItemRepository.saveAll(items);
        saveUsageLog(member.getId(), request.usage());

        return items.stream()
                .map(FridgePhotoItemResponse::from)
                .toList();
    }

    @Transactional
    public FridgePhotoItemResponse updateItem(String email, Long fridgePhotoItemId, FridgePhotoItemUpdateRequest request) {
        ReceiptAnalysisItem item = findOwnedItem(email, fridgePhotoItemId);
        validatePending(item);

        Product product = productRepository.findById(request.productId())
                .filter(Product::getIsActive)
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        item.updateMatchedProduct(
                product.getProductId(),
                product.getName(),
                request.quantity(),
                calculateExpiryDate(product)
        );

        return FridgePhotoItemResponse.from(item);
    }

    @Transactional
    public void rejectItem(String email, Long fridgePhotoItemId) {
        ReceiptAnalysisItem item = findOwnedItem(email, fridgePhotoItemId);
        validatePending(item);
        item.reject();
    }

    @Transactional
    public List<FridgeItemResponse> registerItems(
            String email,
            Long fridgePhotoAnalysisId,
            FridgePhotoItemsRegisterRequest request
    ) {
        Member member = findMemberByEmail(email);
        receiptAnalysisRepository.findByReceiptAnalysisIdAndMemberId(fridgePhotoAnalysisId, member.getId())
                .orElseThrow();

        if (request != null && (request.fridgePhotoItemIds() == null || request.fridgePhotoItemIds().isEmpty())) {
            throw new IllegalArgumentException("등록할 냉장고 사진 후보를 선택해주세요.");
        }

        List<Long> selectedItemIds = request == null ? null : request.fridgePhotoItemIds();
        List<ReceiptAnalysisItem> items = receiptAnalysisItemRepository
                .findByReceiptAnalysisIdOrderByCreatedAtAsc(fridgePhotoAnalysisId)
                .stream()
                .filter(ReceiptAnalysisItem::isPending)
                .filter(item -> selectedItemIds == null
                        || selectedItemIds.contains(item.getReceiptAnalysisItemId()))
                .toList();

        return items.stream()
                .map(item -> registerItem(email, item))
                .toList();
    }

    private FridgeItemResponse registerItem(String email, ReceiptAnalysisItem item) {
        FridgeItemResponse response = fridgeItemService.createFridgeItem(
                email,
                new FridgeItemCreateRequest(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getExpiryDate(),
                        item.getMemo()
                )
        );
        item.register();
        return response;
    }

    private Optional<ReceiptAnalysisItem> createMatchedItem(Long fridgePhotoAnalysisId, FridgePhotoOcrItemRequest item) {
        String normalizedName = resolveAlias(normalizeProductName(item.name()));

        return findMatchedProduct(normalizedName)
                .map(product -> ReceiptAnalysisItem.createPending(
                        fridgePhotoAnalysisId,
                        product.getProductId(),
                        item.name(),
                        normalizedName,
                        product.getName(),
                        defaultQuantity(item.quantity()),
                        calculateExpiryDate(product),
                        "냉장고 사진으로 등록"
                ));
    }

    private void saveUsageLog(Long memberId, AgentLlmUsageResponse usage) {
        if (usage == null) {
            return;
        }

        llmUsageLogService.saveFridgePhotoSuccessLog(
                memberId,
                usage.modelName(),
                usage.promptTokens(),
                usage.completionTokens(),
                usage.totalTokens(),
                usage.estimatedCost()
        );
    }

    private Optional<Product> findMatchedProduct(String normalizedName) {
        Optional<Product> exactMatch = productRepository.findByNameAndIsActiveTrue(normalizedName);
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        return productRepository.findByIsActiveTrue()
                .stream()
                .filter(product -> normalizedName.contains(product.getName()))
                .findFirst();
    }

    private ReceiptAnalysisItem findOwnedItem(String email, Long fridgePhotoItemId) {
        Member member = findMemberByEmail(email);
        ReceiptAnalysisItem item = receiptAnalysisItemRepository.findById(fridgePhotoItemId)
                .orElseThrow();

        receiptAnalysisRepository.findByReceiptAnalysisIdAndMemberId(
                item.getReceiptAnalysisId(),
                member.getId()
        ).orElseThrow();

        return item;
    }

    private void validatePending(ReceiptAnalysisItem item) {
        if (!item.isPending()) {
            throw new IllegalArgumentException("처리 대기 중인 후보만 변경할 수 있습니다.");
        }
    }

    private String normalizeProductName(String name) {
        if (name == null) {
            return "";
        }

        return name
                .replace("국산", "")
                .replace("국내산", "")
                .replace("수입", "")
                .replace("행사", "")
                .replace("할인", "")
                .replaceAll("\\d+(g|kg|ml|l|개|입|봉|팩)", "")
                .trim();
    }

    private String resolveAlias(String normalizedName) {
        String compactName = normalizedName.replaceAll("\\s+", "");
        return PRODUCT_ALIASES.entrySet()
                .stream()
                .filter(entry -> compactName.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(normalizedName);
    }

    private static Map<String, String> createProductAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put("짜파게티", "라면");
        aliases.put("짜짜로니", "라면");
        aliases.put("신라면", "라면");
        aliases.put("진라면", "라면");
        aliases.put("안성탕면", "라면");
        aliases.put("너구리", "라면");
        aliases.put("불닭볶음면", "라면");
        aliases.put("삼양라면", "라면");
        aliases.put("왕뚜껑", "라면");
        aliases.put("육개장사발면", "라면");
        aliases.put("새우탕", "라면");
        aliases.put("튀김우동", "우동");
        aliases.put("생생우동", "우동");
        aliases.put("우동사리", "우동");
        aliases.put("스파게티면", "파스타면");
        aliases.put("파스타", "파스타면");
        aliases.put("당면", "당면");
        aliases.put("컵라면", "라면");
        aliases.put("사발면", "라면");
        aliases.put("햇반", "즉석밥");
        aliases.put("오뚜기밥", "즉석밥");
        aliases.put("즉석밥", "즉석밥");
        aliases.put("컵밥", "즉석밥");
        aliases.put("삼각김밥", "김밥");
        aliases.put("김밥", "김밥");
        aliases.put("스팸", "햄");
        aliases.put("리챔", "햄");
        aliases.put("런천미트", "햄");
        aliases.put("비엔나", "소시지");
        aliases.put("후랑크", "소시지");
        aliases.put("소시지", "소시지");
        aliases.put("베이컨", "베이컨");
        aliases.put("참치캔", "참치");
        aliases.put("동원참치", "참치");
        aliases.put("사조참치", "참치");
        aliases.put("꽁치캔", "꽁치");
        aliases.put("고등어캔", "고등어");
        aliases.put("골뱅이캔", "골뱅이");
        aliases.put("스위트콘", "옥수수");
        aliases.put("옥수수콘", "옥수수");
        aliases.put("비비고만두", "만두");
        aliases.put("고향만두", "만두");
        aliases.put("냉동만두", "만두");
        aliases.put("물만두", "만두");
        aliases.put("군만두", "만두");
        aliases.put("냉동피자", "피자");
        aliases.put("피자", "피자");
        aliases.put("냉동볶음밥", "볶음밥");
        aliases.put("볶음밥", "볶음밥");
        aliases.put("돈까스", "돈까스");
        aliases.put("치킨너겟", "치킨너겟");
        aliases.put("너겟", "치킨너겟");
        aliases.put("떡갈비", "떡갈비");
        aliases.put("핫도그", "핫도그");
        aliases.put("어묵", "어묵");
        aliases.put("맛살", "맛살");
        aliases.put("크래미", "맛살");
        aliases.put("서울우유", "우유");
        aliases.put("매일우유", "우유");
        aliases.put("남양우유", "우유");
        aliases.put("저지방우유", "우유");
        aliases.put("초코우유", "우유");
        aliases.put("딸기우유", "우유");
        aliases.put("요플레", "요거트");
        aliases.put("요거트", "요거트");
        aliases.put("요구르트", "요구르트");
        aliases.put("야쿠르트", "요구르트");
        aliases.put("슬라이스치즈", "치즈");
        aliases.put("체다치즈", "치즈");
        aliases.put("모짜렐라치즈", "치즈");
        aliases.put("버터", "버터");
        aliases.put("케첩", "케첩");
        aliases.put("마요네즈", "마요네즈");
        aliases.put("고추장", "고추장");
        aliases.put("된장", "된장");
        aliases.put("쌈장", "쌈장");
        aliases.put("간장", "간장");
        aliases.put("굴소스", "굴소스");
        aliases.put("참기름", "참기름");
        aliases.put("들기름", "들기름");
        aliases.put("카놀라유", "식용유");
        aliases.put("포도씨유", "식용유");
        aliases.put("올리브유", "식용유");
        aliases.put("식용유", "식용유");
        aliases.put("삼다수", "생수");
        aliases.put("아이시스", "생수");
        aliases.put("백산수", "생수");
        aliases.put("생수", "생수");
        aliases.put("콜라", "탄산음료");
        aliases.put("사이다", "탄산음료");
        aliases.put("탄산음료", "탄산음료");
        aliases.put("오렌지주스", "주스");
        aliases.put("포도주스", "주스");
        aliases.put("주스", "주스");
        aliases.put("커피음료", "커피");
        aliases.put("캔커피", "커피");
        return aliases;
    }

    private String defaultQuantity(String quantity) {
        if (quantity == null || quantity.isBlank()) {
            return "1개";
        }
        return quantity;
    }

    private LocalDate calculateExpiryDate(Product product) {
        if (product.getDefaultExpiryDays() == null) {
            return null;
        }
        return LocalDate.now().plusDays(product.getDefaultExpiryDays());
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidReceiptImageException("냉장고 사진을 업로드해주세요.");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidReceiptImageException("냉장고 사진은 10MB 이하만 업로드할 수 있습니다.");
        }

        String extension = extractExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidReceiptImageException("냉장고 사진은 jpg, jpeg, png 파일만 업로드할 수 있습니다.");
        }
    }

    private String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null) {
            return "";
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    private String createObjectKey(Long memberId, String extension) {
        return "fridge-photos/%d/%s.%s".formatted(
                memberId,
                UUID.randomUUID(),
                extension
        );
    }
}
