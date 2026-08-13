package com.naengpa.naengpamasterbackend.inquiry.knowledge.service;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.request.InquiryKnowledgeDocumentRequest;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response.InquiryKnowledgeContextResponse;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.dto.response.InquiryKnowledgeDocumentResponse;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.entity.InquiryKnowledgeChunk;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.entity.InquiryKnowledgeDocument;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.repository.InquiryKnowledgeChunkRepository;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.repository.InquiryKnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryKnowledgeService {

    // 정책 문서가 작을 때는 고정 길이 청크로 충분하며, 검색 품질이 부족할 때 의미 단위 분할로 교체한다.
    private static final int CHUNK_SIZE = 1_200;
    private static final int MAX_SEARCH_RESULTS = 5;

    private final InquiryKnowledgeDocumentRepository documentRepository;
    private final InquiryKnowledgeChunkRepository chunkRepository;

    // 정책 문서를 등록 또는 갱신하고 검색에 사용할 청크를 다시 생성합니다.
    @Transactional
    public InquiryKnowledgeDocumentResponse saveDocument(InquiryKnowledgeDocumentRequest request) {
        String title = request.title().trim();
        String content = request.content().trim();
        String sourceName = request.sourceName().trim();

        InquiryKnowledgeDocument document = documentRepository.findBySourceName(sourceName)
                .map(existing -> {
                    existing.update(title, content);
                    return existing;
                })
                .orElseGet(() -> documentRepository.save(
                        InquiryKnowledgeDocument.create(title, content, sourceName)
                ));

        List<String> contents = splitIntoChunks(content);
        chunkRepository.deleteAllByDocumentId(document.getId());
        chunkRepository.saveAll(toChunks(document.getId(), contents));

        return InquiryKnowledgeDocumentResponse.from(document, contents.size());
    }

    // 질문과 유사한 정책 문서 청크를 최대 요청 개수만큼 조회합니다.
    @Transactional(readOnly = true)
    public List<InquiryKnowledgeContextResponse> findRelevantContexts(String question, int limit) {
        if (question == null || question.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit, MAX_SEARCH_RESULTS));
        return chunkRepository.findRelevantContexts(question.trim(), safeLimit).stream()
                .map(InquiryKnowledgeContextResponse::from)
                .toList();
    }

    // 문서를 문단 기준으로 묶고 1,200자를 넘는 문단은 고정 길이로 분할합니다.
    static List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();

        for (String paragraph : content.split("\\R\\s*\\R")) {
            String value = paragraph.trim();
            if (value.isEmpty()) {
                continue;
            }

            if (value.length() > CHUNK_SIZE) {
                flush(chunks, chunk);
                for (int start = 0; start < value.length(); start += CHUNK_SIZE) {
                    chunks.add(value.substring(start, Math.min(start + CHUNK_SIZE, value.length())));
                }
                continue;
            }

            int separatorLength = chunk.isEmpty() ? 0 : 2;
            if (chunk.length() + separatorLength + value.length() > CHUNK_SIZE) {
                flush(chunks, chunk);
            }
            if (!chunk.isEmpty()) {
                chunk.append("\n\n");
            }
            chunk.append(value);
        }

        flush(chunks, chunk);
        return chunks;
    }

    // 조립 중인 청크가 있으면 결과 목록에 추가하고 버퍼를 비웁니다.
    private static void flush(List<String> chunks, StringBuilder chunk) {
        if (!chunk.isEmpty()) {
            chunks.add(chunk.toString());
            chunk.setLength(0);
        }
    }

    // 분할된 문자열을 저장 가능한 정책 문서 청크 엔티티로 변환합니다.
    private static List<InquiryKnowledgeChunk> toChunks(Long documentId, List<String> contents) {
        List<InquiryKnowledgeChunk> chunks = new ArrayList<>(contents.size());
        for (int index = 0; index < contents.size(); index++) {
            chunks.add(InquiryKnowledgeChunk.create(documentId, index, contents.get(index)));
        }
        return chunks;
    }
}
