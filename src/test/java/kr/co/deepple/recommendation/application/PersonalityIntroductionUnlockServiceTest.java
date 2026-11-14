package kr.co.deepple.recommendation.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.heart.application.port.in.UseHeartCommand;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.recommendation.adapter.out.persistence.PersonalityIntroductionRedisRepository;
import kr.co.deepple.recommendation.application.dto.PersonalityIntroductionOpenState;
import kr.co.deepple.recommendation.application.port.out.MemberIntroductionCommandRepository;
import kr.co.deepple.recommendation.domain.IntroductionType;
import kr.co.deepple.recommendation.domain.MemberIntroduction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalityIntroductionUnlockServiceTest {

  private static final AnswerPersonalityType TYPE = AnswerPersonalityType.STIMULATING_ADVENTURER;

  @Mock private UseHeartUseCase useHeartUseCase;

  @InjectMocks private PersonalityIntroductionUnlockService personalityIntroductionUnlockService;

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private MemberIntroductionCommandRepository memberIntroductionCommandRepository;

  @Mock private BlockRepository blockRepository;

  @Mock private PersonalityIntroductionRedisRepository personalityIntroductionRedisRepository;

  @Test
  @DisplayName("오픈된 상태가 없으면 예외를 던진다.")
  void throwsWhenNotOpened() {
    long memberId = 1L;
    long targetMemberId = 2L;
    when(personalityIntroductionRedisRepository.findOpenState(memberId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue(
            "errorCode", MemberErrorCode.INVALID_PERSONALITY_INTRODUCTION_UNLOCK);
  }

  @Test
  @DisplayName("오픈된 목록에 없는 대상이면 예외를 던진다.")
  void throwsWhenTargetNotInOpenedList() {
    long memberId = 1L;
    long targetMemberId = 99L;
    when(personalityIntroductionRedisRepository.findOpenState(memberId))
        .thenReturn(
            Optional.of(
                new PersonalityIntroductionOpenState(
                    TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));

    assertThatThrownBy(
            () -> personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue(
            "errorCode", MemberErrorCode.INVALID_PERSONALITY_INTRODUCTION_UNLOCK);
  }

  @Test
  @DisplayName("오픈된 유형과 요청 유형이 다르면 예외를 던진다.")
  void throwsWhenTypeMismatch() {
    long memberId = 1L;
    long targetMemberId = 2L;
    when(personalityIntroductionRedisRepository.findOpenState(memberId))
        .thenReturn(
            Optional.of(
                new PersonalityIntroductionOpenState(
                    AnswerPersonalityType.RATIONAL_REALIST,
                    List.of(2L, 3L, 4L),
                    LocalDateTime.now())));

    assertThatThrownBy(
            () -> personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue(
            "errorCode", MemberErrorCode.INVALID_PERSONALITY_INTRODUCTION_UNLOCK);
  }

  @Test
  @DisplayName("이미 언락된 대상이면 과금 없이 멱등하게 처리한다.")
  void idempotentWhenAlreadyUnlocked() {
    long memberId = 1L;
    long targetMemberId = 2L;
    when(personalityIntroductionRedisRepository.findOpenState(memberId))
        .thenReturn(
            Optional.of(
                new PersonalityIntroductionOpenState(
                    TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));
    when(personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId))
        .thenReturn(Set.of(2L));

    personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId);

    verify(memberIntroductionCommandRepository, never()).save(any());
    verify(personalityIntroductionRedisRepository, never())
        .addUnlockedMemberId(anyLong(), anyLong());
  }

  @Test
  @DisplayName("이미 소개 레코드가 있으면(Redis 유실 등) 과금 없이 멱등하게 처리한다.")
  void idempotentWhenAlreadyIntroducedInDb() {
    long memberId = 1L;
    long targetMemberId = 2L;
    when(personalityIntroductionRedisRepository.findOpenState(memberId))
        .thenReturn(
            Optional.of(
                new PersonalityIntroductionOpenState(
                    TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));
    when(personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId))
        .thenReturn(Set.of());
    when(memberIntroductionCommandRepository.existsByMemberIdAndIntroducedMemberId(
            memberId, targetMemberId))
        .thenReturn(true);

    personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId);

    verify(memberIntroductionCommandRepository, never()).save(any());
  }

  @Test
  @DisplayName("해당 유형의 첫 언락은 하트를 차감하지 않고 소개를 생성한다.")
  void firstUnlockIsFree() {
    long memberId = 1L;
    long targetMemberId = 2L;
    givenValidTarget(memberId, targetMemberId);
    when(personalityIntroductionRedisRepository.findOpenState(memberId))
        .thenReturn(
            Optional.of(
                new PersonalityIntroductionOpenState(
                    TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));
    when(personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId))
        .thenReturn(Set.of());

    try (MockedStatic<MemberIntroduction> mocked = mockStatic(MemberIntroduction.class)) {
      MemberIntroduction introduction = mock(MemberIntroduction.class);
      mocked
          .when(() -> MemberIntroduction.of(memberId, targetMemberId, IntroductionType.PERSONALITY))
          .thenReturn(introduction);

      personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId);

      verify(memberIntroductionCommandRepository).save(introduction);
      verify(useHeartUseCase, never()).useHeart(any());
      verify(personalityIntroductionRedisRepository).addUnlockedMemberId(memberId, targetMemberId);
    }
  }

  @Test
  @DisplayName("첫 언락 이후에는 하트를 차감하고 소개를 생성한다.")
  void subsequentUnlockCharges() {
    long memberId = 1L;
    long targetMemberId = 3L;
    givenValidTarget(memberId, targetMemberId);
    when(personalityIntroductionRedisRepository.findOpenState(memberId))
        .thenReturn(
            Optional.of(
                new PersonalityIntroductionOpenState(
                    TYPE, List.of(2L, 3L, 4L), LocalDateTime.now())));
    when(personalityIntroductionRedisRepository.findUnlockedMemberIds(memberId))
        .thenReturn(Set.of(2L));

    try (MockedStatic<MemberIntroduction> mocked = mockStatic(MemberIntroduction.class)) {
      MemberIntroduction introduction = mock(MemberIntroduction.class);
      when(introduction.getId()).thenReturn(10L);
      mocked
          .when(() -> MemberIntroduction.of(memberId, targetMemberId, IntroductionType.PERSONALITY))
          .thenReturn(introduction);

      personalityIntroductionUnlockService.unlock(memberId, TYPE, targetMemberId);

      verify(memberIntroductionCommandRepository).save(introduction);
      verify(useHeartUseCase)
          .useHeart(
              new UseHeartCommand(
                  memberId,
                  "INTRODUCTION",
                  "PERSONALITY",
                  IntroductionType.PERSONALITY.getDescription(),
                  "10"));
      verify(personalityIntroductionRedisRepository).addUnlockedMemberId(memberId, targetMemberId);
    }
  }

  private void givenValidTarget(long memberId, long targetMemberId) {
    Member target = MemberFixture.builder().id(targetMemberId).build();
    when(memberCommandRepository.findById(targetMemberId)).thenReturn(Optional.of(target));
    when(blockRepository.existsByBlockerIdAndBlockedId(memberId, targetMemberId)).thenReturn(false);
    when(blockRepository.existsByBlockerIdAndBlockedId(targetMemberId, memberId)).thenReturn(false);
  }
}
