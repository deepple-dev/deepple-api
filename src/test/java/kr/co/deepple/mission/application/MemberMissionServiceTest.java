package kr.co.deepple.mission.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.mission.application.port.out.MemberMissionCommandRepository;
import kr.co.deepple.mission.application.port.out.MissionCommandRepository;
import kr.co.deepple.mission.domain.ActionType;
import kr.co.deepple.mission.domain.FrequencyType;
import kr.co.deepple.mission.domain.MemberMission;
import kr.co.deepple.mission.domain.Mission;
import kr.co.deepple.mission.domain.TargetGender;
import kr.co.deepple.mission.event.MemberMissionCompletedEvent;
import kr.co.deepple.mission.fixture.MemberMissionFixture;
import kr.co.deepple.mission.fixture.MissionFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class MemberMissionServiceTest {

  @Mock private MemberMissionCommandRepository memberMissionCommandRepository;

  @Mock private MissionCommandRepository missionCommandRepository;

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private MemberMissionService memberMissionService;

  @Nested
  @DisplayName("미션 수행")
  class doMemberMission {

    Mission mission;
    Member member;
    MemberMission memberMission;

    long memberId = 1L;
    long missionId = 2L;
    long memberMissionId = 3L;

    int requiredAttempt = 1; // 1회 하면 미션 성공.
    int repeatableCount = 2; // 2번까지 반복 가능.
    int rewardedHeart = 4; // 하트 보상 4개.
    boolean isPublic = true; // 미션 공개.

    @BeforeEach
    void setUp() {
      mission =
          MissionFixture.builder()
              .id(missionId)
              .actionType(ActionType.LIKE)
              .frequencyType(FrequencyType.DAILY)
              .targetGender(TargetGender.MALE)
              .requiredAttempt(requiredAttempt)
              .repeatableCount(repeatableCount)
              .rewardedHeart(rewardedHeart)
              .publicMission(true)
              .build();
      member = MemberFixture.builder().id(memberId).gender(Gender.MALE).nickname("닉네임").build();
      memberMission =
          MemberMissionFixture.builder()
              .id(memberMissionId)
              .memberId(memberId)
              .missionId(missionId)
              .build();
    }

    @Test
    @DisplayName("멤버가 존재하지 않는 경우, 예외를 발생합니다.")
    void throwExceptionWhenMemberIsNotFound() {
      // Given
      Mockito.when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(() ->
              memberMissionService.executeMissionsByAction(
                  memberId, mission.getActionType().name()))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("미션에 성공하면 회원 미션 ID와 성공 횟수를 담은 보상 이벤트를 발행합니다.")
    void raiseEventWhenSucceeded() {
      // Given
      givenMissionAndMember(mission);
      givenMemberMission(mission, memberMission);

      // When
      boolean hasProcessedMission =
          memberMissionService.executeMissionsByAction(memberId, mission.getActionType().name());

      // Then
      assertThat(hasProcessedMission).isTrue();
      assertThat(memberMission.getSuccessCount()).isEqualTo(1);
      Mockito.verify(eventPublisher)
          .publishEvent(
              ArgumentMatchers.<MemberMissionCompletedEvent>argThat(event ->
                  event.memberId() == memberId
                      && event.memberMissionId() == memberMissionId
                      && event.successCount() == 1
                      && event.rewardHeartAmount() == rewardedHeart));
    }

    @Test
    @DisplayName("필요한 시도 횟수를 채우지 못하면 보상 이벤트를 발행하지 않습니다.")
    void notRaiseEventWhenNotSucceeded() {
      // Given
      Mission twoAttemptMission =
          MissionFixture.builder()
              .id(missionId)
              .actionType(ActionType.LIKE)
              .frequencyType(FrequencyType.DAILY)
              .targetGender(TargetGender.MALE)
              .requiredAttempt(2)
              .build();
      givenMissionAndMember(twoAttemptMission);
      givenMemberMission(twoAttemptMission, memberMission);

      // When
      boolean hasProcessedMission =
          memberMissionService.executeMissionsByAction(
              memberId, twoAttemptMission.getActionType().name());

      // Then
      assertThat(hasProcessedMission).isTrue();
      assertThat(memberMission.getAttemptCount()).isEqualTo(1);
      Mockito.verify(eventPublisher, Mockito.never()).publishEvent(Mockito.any(Object.class));
    }

    @Test
    @DisplayName("챌린지 미션은 날짜와 관계없는 기간으로 회원 미션을 찾습니다.")
    void findChallengeMemberMissionByWholePeriod() {
      // Given
      Mission challengeMission =
          MissionFixture.builder()
              .id(missionId)
              .actionType(ActionType.INTERVIEW)
              .frequencyType(FrequencyType.CHALLENGE)
              .targetGender(TargetGender.MALE)
              .build();
      givenMissionAndMember(challengeMission);
      Mockito.when(memberMissionCommandRepository.findOrCreateForUpdate(memberId, missionId, "ALL"))
          .thenReturn(memberMission);

      // When
      memberMissionService.executeMissionsByAction(
          memberId, challengeMission.getActionType().name());

      // Then
      Mockito.verify(memberMissionCommandRepository)
          .findOrCreateForUpdate(memberId, missionId, "ALL");
    }

    @Test
    @DisplayName("멤버가 해당 미션을 완료한 경우, 해당 멤버-미션을 수행하지 않습니다.")
    void notDoMemberMissionWhenIsCompleted() {
      // Given
      MemberMission completedMemberMission =
          MemberMissionFixture.builder()
              .memberId(memberId)
              .missionId(missionId)
              .successCount(repeatableCount)
              .completed(true)
              .build();
      givenMissionAndMember(mission);
      givenMemberMission(mission, completedMemberMission);

      // When
      boolean hasProcessedMission =
          memberMissionService.executeMissionsByAction(memberId, mission.getActionType().name());

      // Then
      assertThat(completedMemberMission.getSuccessCount()).isEqualTo(repeatableCount);
      assertThat(completedMemberMission.getAttemptCount()).isZero();
      assertThat(hasProcessedMission).isFalse();
      Mockito.verify(eventPublisher, Mockito.never()).publishEvent(Mockito.any(Object.class));
    }

    private void givenMissionAndMember(Mission mission) {
      Mockito.when(
              missionCommandRepository.findByActionTypeAndTargetGender(
                  mission.getActionType(), mission.getTargetGender()))
          .thenReturn(List.of(mission));
      Mockito.when(
              missionCommandRepository.findByActionTypeAndTargetGender(
                  mission.getActionType(), TargetGender.ALL))
          .thenReturn(List.of());
      Mockito.when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));
    }

    private void givenMemberMission(Mission mission, MemberMission memberMission) {
      Mockito.when(
              memberMissionCommandRepository.findOrCreateForUpdate(
                  memberId, missionId, LocalDate.now().toString()))
          .thenReturn(memberMission);
    }
  }
}
