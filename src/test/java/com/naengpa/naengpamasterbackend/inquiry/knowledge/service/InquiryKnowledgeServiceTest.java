package com.naengpa.naengpamasterbackend.inquiry.knowledge.service;

import com.naengpa.naengpamasterbackend.inquiry.knowledge.repository.InquiryKnowledgeChunkRepository;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.repository.InquiryKnowledgeContextProjection;
import com.naengpa.naengpamasterbackend.inquiry.knowledge.repository.InquiryKnowledgeDocumentRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InquiryKnowledgeServiceTest {

    @Test
    void splitIntoChunksKeepsEveryChunkWithinSizeLimit() {
        String longParagraph = "가".repeat(1_300);
        String content = "# 회원 정책\n\n로그인이 필요합니다.\n\n" + longParagraph;

        List<String> chunks = InquiryKnowledgeService.splitIntoChunks(content);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks).allMatch(chunk -> chunk.length() <= 1_200);
        assertThat(String.join("", chunks).replace("\n", ""))
                .contains("회원 정책", "로그인이 필요합니다.", longParagraph);
    }

    @Test
    void searchTrimsQuestionAndLimitsTopKToFive() {
        InquiryKnowledgeChunkRepository chunkRepository = mock(InquiryKnowledgeChunkRepository.class);
        InquiryKnowledgeContextProjection projection = mock(InquiryKnowledgeContextProjection.class);
        InquiryKnowledgeService service = new InquiryKnowledgeService(
                mock(InquiryKnowledgeDocumentRepository.class),
                chunkRepository
        );
        when(projection.getSourceName()).thenReturn("inquiry.md");
        when(projection.getContent()).thenReturn("문의 탭에서 등록합니다.");
        when(chunkRepository.findRelevantContexts("문의 등록", 5)).thenReturn(List.of(projection));

        var contexts = service.findRelevantContexts("  문의 등록  ", 100);

        assertThat(contexts).hasSize(1);
        assertThat(contexts.getFirst().sourceName()).isEqualTo("inquiry.md");
        verify(chunkRepository).findRelevantContexts("문의 등록", 5);
    }

    @Test
    void blankQuestionDoesNotSearchDatabase() {
        InquiryKnowledgeChunkRepository chunkRepository = mock(InquiryKnowledgeChunkRepository.class);
        InquiryKnowledgeService service = new InquiryKnowledgeService(
                mock(InquiryKnowledgeDocumentRepository.class),
                chunkRepository
        );

        assertThat(service.findRelevantContexts("  ", 5)).isEmpty();
    }
}
