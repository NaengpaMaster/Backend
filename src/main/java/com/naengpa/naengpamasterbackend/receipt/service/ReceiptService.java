package com.naengpa.naengpamasterbackend.receipt.service;

import com.naengpa.naengpamasterbackend.agent.shopping.client.dto.AgentLlmUsageResponse;
import com.naengpa.naengpamasterbackend.agent.usage.service.LlmUsageLogService;
import com.naengpa.naengpamasterbackend.global.exception.InvalidReceiptImageException;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.global.s3.S3Uploader;
import com.naengpa.naengpamasterbackend.fridge.dto.request.FridgeItemCreateRequest;
import com.naengpa.naengpamasterbackend.fridge.dto.response.FridgeItemResponse;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeItemService;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.product.entity.Product;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import com.naengpa.naengpamasterbackend.product.repository.ProductRepository;
import com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptAnalysisItemUpdateRequest;
import com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptFridgeRegisterRequest;
import com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptOcrItemRequest;
import com.naengpa.naengpamasterbackend.receipt.dto.request.ReceiptOcrSaveRequest;
import com.naengpa.naengpamasterbackend.receipt.dto.response.ReceiptAnalysisItemResponse;
import com.naengpa.naengpamasterbackend.receipt.dto.response.ReceiptImageUploadResponse;
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
public class ReceiptService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Map<String, String> PRODUCT_ALIASES = createProductAliases();


    private final MemberRepository memberRepository;
    private final ReceiptAnalysisRepository receiptAnalysisRepository;
    private final S3Uploader s3Uploader;
    private final ReceiptAnalysisItemRepository receiptAnalysisItemRepository;
    private final ProductRepository productRepository;
    private final FridgeItemService fridgeItemService;
    private final LlmUsageLogService llmUsageLogService;

    public ReceiptService(
            MemberRepository memberRepository,
            ReceiptAnalysisRepository receiptAnalysisRepository,
            S3Uploader s3Uploader,
            ReceiptAnalysisItemRepository receiptAnalysisItemRepository,
            ProductRepository productRepository,
            FridgeItemService fridgeItemService,
            LlmUsageLogService llmUsageLogService
    ) {
        this.memberRepository = memberRepository;
        this.receiptAnalysisRepository = receiptAnalysisRepository;
        this.s3Uploader = s3Uploader;
        this.receiptAnalysisItemRepository = receiptAnalysisItemRepository;
        this.productRepository = productRepository;
        this.fridgeItemService = fridgeItemService;
        this.llmUsageLogService = llmUsageLogService;
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

    @Transactional(readOnly = true)
    public List<ReceiptAnalysisItemResponse> getReceiptAnalysisItems(
            String email,
            Long receiptAnalysisId
    ) {
        Member member = findMemberByEmail(email);

        // receiptAnalysisId만으로 조회하면 타인의 영수증 후보가 노출될 수 있어 회원 ID까지 함께 확인
        receiptAnalysisRepository.findByReceiptAnalysisIdAndMemberId(
                receiptAnalysisId,
                member.getId()
        ).orElseThrow();

        // 검증된 영수증 분석 ID에 연결된 OCR 후보 항목을 생성순으로 반환
        return receiptAnalysisItemRepository
                .findByReceiptAnalysisIdOrderByCreatedAtAsc(receiptAnalysisId)
                .stream()
                .map(ReceiptAnalysisItemResponse::from)
                .toList();
    }

    @Transactional
    public List<ReceiptAnalysisItemResponse> saveOcrResult(
            String email,
            Long receiptAnalysisId,
            ReceiptOcrSaveRequest request
    ) {
        Member member = findMemberByEmail(email);

        ReceiptAnalysis receiptAnalysis = receiptAnalysisRepository
                .findByReceiptAnalysisIdAndMemberId(receiptAnalysisId, member.getId())
                .orElseThrow();

        // Agent가 읽어낸 영수증 전체 원문을 분석 row에 저장해 추후 매칭 실패 원인을 확인할 수 있게 함
        receiptAnalysis.updateRawOcrText(request.rawText());

        // OCR 후보 중 사전 재료와 매칭된 항목만 냉장고 등록 후보로 저장
        List<ReceiptAnalysisItem> items = request.items().stream()
                .map(item -> createMatchedReceiptItem(receiptAnalysisId, item))
                .flatMap(Optional::stream)
                .toList();

        receiptAnalysisItemRepository.saveAll(items);
        saveUsageLog(member.getId(), request.usage());

        return items.stream()
                .map(ReceiptAnalysisItemResponse::from)
                .toList();
    }

    @Transactional
    public ReceiptAnalysisItemResponse updateReceiptAnalysisItem(
            String email,
            Long receiptItemId,
            ReceiptAnalysisItemUpdateRequest request
    ) {
        ReceiptAnalysisItem item = findOwnedReceiptItem(email, receiptItemId);
        validatePending(item);

        // 사용자가 직접 고른 사전 재료로 후보 매칭 정보를 교체
        Product product = productRepository.findById(request.productId())
                .filter(Product::getIsActive)
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        item.updateMatchedProduct(
                product.getProductId(),
                product.getName(),
                request.quantity(),
                calculateExpiryDate(product)
        );

        return ReceiptAnalysisItemResponse.from(item);
    }

    @Transactional
    public void rejectReceiptAnalysisItem(String email, Long receiptItemId) {
        ReceiptAnalysisItem item = findOwnedReceiptItem(email, receiptItemId);
        validatePending(item);
        // 제외된 후보는 이후 냉장고 일괄 등록 대상에서 빠짐
        item.reject();
    }

    @Transactional
    public List<FridgeItemResponse> registerReceiptItemsToFridge(
            String email,
            Long receiptAnalysisId,
            ReceiptFridgeRegisterRequest request
    ) {
        Member member = findMemberByEmail(email);
        receiptAnalysisRepository.findByReceiptAnalysisIdAndMemberId(receiptAnalysisId, member.getId())
                .orElseThrow();

        if (request != null && (request.receiptItemIds() == null || request.receiptItemIds().isEmpty())) {
            throw new IllegalArgumentException("등록할 영수증 후보를 선택해주세요.");
        }

        // request가 없으면 전체 PENDING 후보, 선택 목록이 있으면 선택한 후보만 등록
        List<Long> selectedItemIds = request == null ? null : request.receiptItemIds();
        List<ReceiptAnalysisItem> items = receiptAnalysisItemRepository
                .findByReceiptAnalysisIdOrderByCreatedAtAsc(receiptAnalysisId)
                .stream()
                .filter(ReceiptAnalysisItem::isPending)
                .filter(item -> selectedItemIds == null
                        || selectedItemIds.contains(item.getReceiptAnalysisItemId()))
                .toList();

        return items.stream()
                .map(item -> registerReceiptItem(email, item))
                .toList();
    }

    private FridgeItemResponse registerReceiptItem(String email, ReceiptAnalysisItem item) {
        // 냉장고 등록 검증 정책을 유지하기 위해 기존 FridgeItemService를 그대로 재사용
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

    private ReceiptAnalysisItem findOwnedReceiptItem(String email, Long receiptItemId) {
        Member member = findMemberByEmail(email);
        ReceiptAnalysisItem item = receiptAnalysisItemRepository.findById(receiptItemId)
                .orElseThrow();

        // 후보 항목 자체에는 memberId가 없으므로 상위 receipt_analysis로 소유자 검증
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

    private Optional<ReceiptAnalysisItem> createMatchedReceiptItem(
            Long receiptAnalysisId,
            ReceiptOcrItemRequest item
    ) {
        // OCR 상품명에서 원산지/용량 등 매칭에 방해되는 단어를 먼저 제거
        String normalizedName = resolveAlias(normalizeProductName(item.name()));

        return findMatchedProduct(normalizedName)
                .map(product -> ReceiptAnalysisItem.createPending(
                        receiptAnalysisId,
                        product.getProductId(),
                        item.name(),
                        normalizedName,
                        product.getName(),
                        defaultQuantity(item.quantity()),
                        calculateExpiryDate(product)
                ));
    }

    private void saveUsageLog(Long memberId, AgentLlmUsageResponse usage) {
        if (usage == null) {
            return;
        }

        llmUsageLogService.saveReceiptOcrSuccessLog(
                memberId,
                usage.modelName(),
                usage.promptTokens(),
                usage.completionTokens(),
                usage.totalTokens(),
                usage.estimatedCost()
        );
    }

    private Optional<Product> findMatchedProduct(String normalizedName) {
        // 1순위는 정확히 같은 사전 재료명, 실패하면 OCR 상품명 안에 포함된 사전 재료명을 찾음
        Optional<Product> exactMatch = productRepository.findByNameAndIsActiveTrue(normalizedName);
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        return productRepository.findByIsActiveTrue()
                .stream()
                .filter(product -> normalizedName.contains(product.getName()))
                .findFirst();
    }

    private String normalizeProductName(String name) {
        if (name == null) {
            return "";
        }

        // MVP 정제 규칙: 자주 붙는 원산지/행사 문구와 단순 용량 표기를 제거
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
        // 사전 재료의 기본 유통기한이 있으면 오늘 기준으로 냉장고 등록 후보 유통기한을 미리 계산
        return LocalDate.now().plusDays(product.getDefaultExpiryDays());
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
