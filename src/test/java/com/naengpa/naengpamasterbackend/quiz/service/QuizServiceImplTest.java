package com.naengpa.naengpamasterbackend.quiz.service;

import com.naengpa.naengpamasterbackend.global.exception.QuizAlreadySolvedException;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import com.naengpa.naengpamasterbackend.quiz.QuizServiceImpl;
import com.naengpa.naengpamasterbackend.quiz.dto.request.QuizSubmitRequest;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizSubmitResponse;
import com.naengpa.naengpamasterbackend.quiz.dto.response.QuizTodayResponse;
import com.naengpa.naengpamasterbackend.quiz.entity.Quiz;
import com.naengpa.naengpamasterbackend.quiz.repository.QuizRepository;
import com.naengpa.naengpamasterbackend.quiz.repository.QuizResultRepository;
import com.naengpa.naengpamasterbackend.score.entity.ScoreReason;
import com.naengpa.naengpamasterbackend.score.service.ScoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock QuizRepository quizRepository;
    @Mock QuizResultRepository quizResultRepository;
    @Mock MemberRepository memberRepository;
    @Mock ScoreService scoreService;

    @InjectMocks
    QuizServiceImpl quizService;

    private Quiz sampleQuiz() {
        return Quiz.create("상추는 신문지로 감싸서 냉장 보관하면 더 오래간다.",
                true, "상추는 수분 손실이 빠른 채소입니다.", "상추", 1L, LocalDate.now());
    }

    private Member sampleMember() {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(7L);
        return member;
    }

    @Test
    @DisplayName("오늘의 퀴즈가 없으면 준비중 상태로 응답한다")
    void getTodayQuiz_noQuiz() {
        //given: 어떤 상황을 가정할지 준비
        given(quizRepository.findByQuizDate(LocalDate.now())).willReturn(Optional.empty());
        //when: 실제로 테스트하려는 동작 실행
        QuizTodayResponse response = quizService.getTodayQuiz("test@email.com");
        //then: 결과가 예상대로인지 확인
        assertThat(response.quizId()).isNull();
        assertThat(response.alreadySolved()).isTrue();
    }

    @Test
    @DisplayName("정답을 제출하면 정답 처리되고 점수가 반영된다")
    void submitQuiz_correctAnswer() {
        Quiz quiz = sampleQuiz();
        Member member = sampleMember();

        given(memberRepository.findByEmail("test@email.com")).willReturn(Optional.of(member));
        given(quizRepository.findById(1L)).willReturn(Optional.of(quiz));
        given(quizResultRepository.existsByMemberIdAndQuizId(7L, quiz.getQuizId())).willReturn(false);

        QuizSubmitResponse response = quizService.submitQuiz("test@email.com",
                new QuizSubmitRequest(1L, true));

        assertThat(response.isCorrect()).isTrue();
        assertThat(response.scoreDelta()).isEqualTo(2);
        verify(scoreService).addScore(7L, ScoreReason.QUIZ_CORRECT, null, null, 2);
    }

    @Test
    @DisplayName("오답을 제출하면 점수가 반영되지 않는다")
    void submitQuiz_wrongAnswer() {
        Quiz quiz = sampleQuiz();
        Member member = sampleMember();

        given(memberRepository.findByEmail("test@email.com")).willReturn(Optional.of(member));
        given(quizRepository.findById(1L)).willReturn(Optional.of(quiz));
        given(quizResultRepository.existsByMemberIdAndQuizId(7L, quiz.getQuizId())).willReturn(false);

        QuizSubmitResponse response = quizService.submitQuiz("test@email.com",
                new QuizSubmitRequest(1L, false));

        assertThat(response.isCorrect()).isFalse();
        assertThat(response.scoreDelta()).isNull();
    }

    @Test
    @DisplayName("이미 제출한 퀴즈에 재제출하면 예외가 발생한다")
    void submitQuiz_alreadySolved() {
        Quiz quiz = sampleQuiz();
        Member member = sampleMember();

        given(memberRepository.findByEmail("test@email.com")).willReturn(Optional.of(member));
        given(quizRepository.findById(1L)).willReturn(Optional.of(quiz));
        given(quizResultRepository.existsByMemberIdAndQuizId(7L, quiz.getQuizId())).willReturn(true);

        assertThatThrownBy(() ->
                quizService.submitQuiz("test@email.com", new QuizSubmitRequest(1L, true))
        ).isInstanceOf(QuizAlreadySolvedException.class);
    }
}