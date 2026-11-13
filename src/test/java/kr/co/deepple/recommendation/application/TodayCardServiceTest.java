package kr.co.deepple.recommendation.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.recommendation.application.port.out.MemberIntroductionCommandRepository;
import kr.co.deepple.recommendation.domain.IntroductionType;
import kr.co.deepple.recommendation.domain.MemberIntroduction;
import kr.co.deepple.recommendation.fixture.MemberIntroductionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TodayCardServiceTest {

  @Mock private UseHeartUseCase useHeartUseCase;

  @InjectMocks private TodayCardService todayCardService;

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private MemberIntroductionCommandRepository memberIntroductionCommandRepository;

  @Test
  @DisplayName("오늘의 카드 멤버 id가 모두 이미 소개된 멤버라면 소개를 생성하지 않는다")
  void doesNotCreateIntroductionWhenAlreadyExists() {
    // given
    long memberId = 1L;
    Set<Long> todayCardMemberIds = Set.of(2L, 3L);

    MemberIntroduction memberIntroduction1 =
        MemberIntroductionFixture.builder().memberId(memberId).introducedMemberId(2L).build();
    MemberIntroduction memberIntroduction2 =
        MemberIntroductionFixture.builder().memberId(memberId).introducedMemberId(3L).build();

    when(memberIntroductionCommandRepository.findAllByMemberIdAndIntroducedMemberIdIn(
            memberId, todayCardMemberIds))
        .thenReturn(List.of(memberIntroduction1, memberIntroduction2));

    // when
    todayCardService.createTodayCardIntroductions(memberId, todayCardMemberIds);

    // then
    verify(memberCommandRepository, never()).findAllById(any());
    verify(memberIntroductionCommandRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("오늘의 카드 멤버 id 중에 소개되지 않은 멤버가 존재하지 않는다면 소개를 생성하지 않는다")
  void doesNotCreateIntroductionWhenNoIntroducedMembers() {
    // given
    long memberId = 1L;
    Set<Long> todayCardMemberIds = Set.of(2L, 3L);

    when(memberIntroductionCommandRepository.findAllByMemberIdAndIntroducedMemberIdIn(
            memberId, todayCardMemberIds))
        .thenReturn(List.of());
    when(memberCommandRepository.findAllById(todayCardMemberIds)).thenReturn(List.of());

    // when
    todayCardService.createTodayCardIntroductions(memberId, todayCardMemberIds);

    // then
    verify(memberIntroductionCommandRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("오늘의 카드 멤버 id 중에 소개되지 않은 멤버 비활성화되어 있다면 소개를 생성하지 않는다")
  void doesNotCreateIntroductionWhenIntroducedMemberIsInactive() {
    // given
    long memberId = 1L;
    Set<Long> todayCardMemberIds = Set.of(2L, 3L, 4L);

    when(memberIntroductionCommandRepository.findAllByMemberIdAndIntroducedMemberIdIn(
            memberId, todayCardMemberIds))
        .thenReturn(List.of());

    Member inactiveMember = MemberFixture.builder().activityStatus(ActivityStatus.DORMANT).build();

    when(memberCommandRepository.findAllById(todayCardMemberIds))
        .thenReturn(List.of(inactiveMember));

    // when
    todayCardService.createTodayCardIntroductions(memberId, todayCardMemberIds);

    // then
    verify(memberIntroductionCommandRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("오늘의 카드 멤버 id 중에 소개되지 않은 멤버가 active 상태라면 소개를 생성한다")
  void createsIntroductionWhenIntroducedMemberIsActive() {
    // given
    long memberId = 1L;
    Set<Long> todayCardMemberIds = Set.of(2L, 3L);

    when(memberIntroductionCommandRepository.findAllByMemberIdAndIntroducedMemberIdIn(
            memberId, todayCardMemberIds))
        .thenReturn(List.of());

    Member activeMember = MemberFixture.builder().id(2L).build();

    when(memberCommandRepository.findAllById(todayCardMemberIds)).thenReturn(List.of(activeMember));

    // when
    try (MockedStatic<MemberIntroduction> memberIntroductionMockedStatic =
        mockStatic(MemberIntroduction.class)) {
      MemberIntroduction memberIntroduction = mock(MemberIntroduction.class);
      memberIntroductionMockedStatic
          .when(() ->
              MemberIntroduction.of(memberId, activeMember.getId(), IntroductionType.TODAY_CARD))
          .thenReturn(memberIntroduction);
      todayCardService.createTodayCardIntroductions(memberId, todayCardMemberIds);

      // then
      verify(memberIntroductionCommandRepository, times(1)).saveAll(List.of(memberIntroduction));
    }
  }
}
