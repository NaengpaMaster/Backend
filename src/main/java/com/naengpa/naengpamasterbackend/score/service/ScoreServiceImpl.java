package com.naengpa.naengpamasterbackend.score.service;

import com.naengpa.naengpamasterbackend.global.exception.ScoreNotFoundException;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.score.dto.response.ScoreByReasonResponse;
import com.naengpa.naengpamasterbackend.score.dto.response.ScoreHistoryResponse;
import com.naengpa.naengpamasterbackend.score.dto.response.ScoreResponse;
import com.naengpa.naengpamasterbackend.score.dto.response.ScoreSummaryResponse;
import com.naengpa.naengpamasterbackend.score.entity.Score;
import com.naengpa.naengpamasterbackend.score.entity.ScoreHistory;
import com.naengpa.naengpamasterbackend.score.entity.ScoreReason;
import com.naengpa.naengpamasterbackend.score.repository.ScoreAnalysisRepository;
import com.naengpa.naengpamasterbackend.score.repository.ScoreHistoryRepository;
import com.naengpa.naengpamasterbackend.score.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final ScoreRepository scoreRepository;
    private final MemberRepository memberRepository;
    private final ScoreHistoryRepository scoreHistoryRepository;
    private final ScoreAnalysisRepository scoreAnalysisRepository;

    @Override
    public ScoreResponse getScore(String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        Score score = scoreRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new ScoreNotFoundException());

        return new ScoreResponse(score.getScore());
    }

    @Override
    public Page<ScoreHistoryResponse> getScoreHistories(String email, Pageable pageable) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        Page<ScoreHistory> histories = scoreHistoryRepository
                .findByMemberIdOrderByCreatedAtDesc(member.getId(), pageable);

        return histories.map(h -> new ScoreHistoryResponse(
                        h.getScoreReason(),
                        h.getTargetName(),
                        h.getProductCategoryId(), // 추가
                        h.getScoreDelta(),
                        h.getCreatedAt()
                ));
    }

    @Override
    @Transactional
    public void addScore(Long memberId, ScoreReason reason, String targetName, Long targetId, int delta) {

        Score score = scoreRepository.findByMemberId(memberId)
                .orElseThrow(ScoreNotFoundException::new);

        int appliedDelta = score.addScore(delta);

        scoreHistoryRepository.save(
                ScoreHistory.create(memberId, reason, targetName, targetId, null, appliedDelta)
        );
    }

    @Override
    public List<ScoreByReasonResponse> getScoreByReason(String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return scoreAnalysisRepository.findScoreSummaryByReason(member.getId(), startOfMonth);
    }

    @Override
    public ScoreSummaryResponse getSummary(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return scoreAnalysisRepository.findSummary(member.getId(), startOfMonth);
    }

    @Override
    public ScoreByReasonResponse getHighlight(String email){

        List<ScoreByReasonResponse> byReason = getScoreByReason(email);

        return byReason.stream()
                .max(Comparator.comparingLong(r -> Math.abs(r.totalDelta())))
                .orElse(null);
    }

}
