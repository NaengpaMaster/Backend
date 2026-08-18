package com.naengpa.naengpamasterbackend.quiz.service;

import com.naengpa.naengpamasterbackend.global.exception.QuizAlreadySolvedException;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.quiz.dto.request.QuizSubmitRequest;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizSubmitResponse;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizTodayResponse;
import com.naengpa.naengpamasterbackend.quiz.entity.Quiz;
import com.naengpa.naengpamasterbackend.quiz.entity.QuizResult;
import com.naengpa.naengpamasterbackend.quiz.repository.QuizRepository;
import com.naengpa.naengpamasterbackend.quiz.repository.QuizResultRepository;
import com.naengpa.naengpamasterbackend.score.entity.ScoreReason;
import com.naengpa.naengpamasterbackend.score.service.ScoreService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final MemberRepository memberRepository;
    private final ScoreService scoreService;

    @Override
    public QuizTodayResponse getTodayQuiz(String email) {

        Optional<Quiz> quizQpt = quizRepository.findByQuizDate(LocalDate.now());

        if (quizQpt.isEmpty()) {
            // 오늘의 퀴즈 미생성 상태
            return new QuizTodayResponse(null, null, null, true, null, null, null);
        }

        Quiz quiz = quizQpt.get();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow( () -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        Optional<QuizResult> resultOpt = quizResultRepository.findByMemberIdAndQuizId(member.getId(), quiz.getQuizId());

        if(resultOpt.isPresent()){
            QuizResult result = resultOpt.get();
            return new QuizTodayResponse(
                    quiz.getQuizId(),
                    quiz.getStatement(),
                    quiz.getSourceProductName(),
                    true,
                    result.getSubmittedAnswer(),
                    result.getIsCorrect(),
                    quiz.getExplanation()
            );
        }

        return new QuizTodayResponse(
                quiz.getQuizId(),
                quiz.getStatement(),
                quiz.getSourceProductName(),
                false,
                null,
                null,
                null
        );
    }

    @Override
    @Transactional
    public QuizSubmitResponse submitQuiz(String email, QuizSubmitRequest request) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow( () -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));

        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(()-> new IllegalArgumentException("퀴즈를 찾을 수 없습니다."));

        // 날짜 검증
        if (!quiz.getQuizDate().equals(LocalDate.now())){
            throw new QuizAlreadySolvedException("퀴즈가 갱신되었어요. 새로고침 후 다시 풀어주세요.");
        }

        //중복 제출 방지
        if (quizResultRepository.existsByMemberIdAndQuizId(member.getId(), quiz.getQuizId())){
            throw new QuizAlreadySolvedException("이미 오늘 퀴즈를 풀었어요.");
        }

        //정답 판정
        boolean isCorrect = request.submittedAnswer().equals(quiz.getAnswer());

        //결과 저장
        quizResultRepository.save(
                QuizResult.create(member.getId(), quiz.getQuizId(), request.submittedAnswer(), isCorrect)
        );

        //정답이면 점수 반영
        Integer scoreDelta = null;
        if (isCorrect){
            scoreDelta = 2;
            scoreService.addScore(member.getId(), ScoreReason.QUIZ_CORRECT, null, null, scoreDelta);
        }

        return new QuizSubmitResponse(isCorrect, quiz.getExplanation(), scoreDelta);

    }
}
