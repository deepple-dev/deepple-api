package deepple.deepple.mission.command.application.memberMission;

import deepple.deepple.common.MockEventsExtension;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Gender;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.fixture.MemberFixture;
import deepple.deepple.mission.command.domain.memberMission.MemberMission;
import deepple.deepple.mission.command.domain.memberMission.MemberMissionCommandRepository;
import deepple.deepple.mission.command.domain.mission.*;
import deepple.deepple.mission.fixture.MemberMissionFixture;
import deepple.deepple.mission.fixture.MissionFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith({MockEventsExtension.class, MockitoExtension.class})
public class MemberMissionServiceTest {

    @Mock
    private MemberMissionCommandRepository memberMissionCommandRepository;

    @Mock
    private MissionCommandRepository missionCommandRepository;

    @Mock
    private MemberCommandRepository memberCommandRepository;

    @InjectMocks
    private MemberMissionService memberMissionService;


    @Nested
    @DisplayName("미션 수행")
    class doMemberMission {
        Mission mission;
        Member member;
        MemberMission memberMission;

        long memberId = 1L;
        long missionId = 2L;

        int requiredAttempt = 1; // 1회 하면 미션 성공.
        int repeatableCount = 2; // 2번까지 반복 가능.
        int rewardedHeart = 4; // 하트 보상 4개.
        boolean isPublic = true; // 미션 공개.

        @BeforeEach
        void setUp() {
            mission = MissionFixture.builder()
                .id(missionId)
                .actionType(ActionType.LIKE)
                .frequencyType(FrequencyType.DAILY)
                .targetGender(TargetGender.MALE)
                .requiredAttempt(requiredAttempt)
                .repeatableCount(repeatableCount)
                .rewardedHeart(rewardedHeart)
                .publicMission(true)
                .build();
            member = MemberFixture.builder()
                .id(memberId)
                .gender(Gender.MALE)
                .nickname("닉네임")
                .build();
            memberMission = MemberMissionFixture.builder()
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
            Assertions.assertThatThrownBy(
                    () -> memberMissionService.executeMissionsByAction(memberId, mission.getActionType().name()))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
        }


        @Test
        @DisplayName("멤버의 미션이 존재하지 않는 경우, 새롭게 생성합니다.")
        void createMemberMissionWhenNotExists() {
            // Given
            Mockito.when(missionCommandRepository.findByActionTypeAndTargetGender(mission.getActionType(),
                    mission.getTargetGender()))
                .thenReturn(List.of(mission));
            Mockito.when(missionCommandRepository.findByActionTypeAndTargetGender(mission.getActionType(),
                    TargetGender.ALL))
                .thenReturn(List.of());
            Mockito.when(memberCommandRepository.findById(memberId))
                .thenReturn(Optional.of(member));
            Mockito.when(memberMissionCommandRepository.findByMemberIdAndMissionIdOnToday(memberId, missionId))
                .thenReturn(Optional.empty());
            Mockito.when(memberMissionCommandRepository.save(Mockito.any()))
                .thenReturn(memberMission);

            // When
            memberMissionService.executeMissionsByAction(memberId, mission.getActionType().name());

            // Then
            Mockito.verify(memberMissionCommandRepository).save(Mockito.any(MemberMission.class));
        }

        @Test
        @DisplayName("멤버의 미션이 존재하는 경우, 해당 멤버-미션을 수행합니다.")
        void doMemberMissionWhenExists() {
            // Given
            Mockito.when(missionCommandRepository.findByActionTypeAndTargetGender(mission.getActionType(),
                    mission.getTargetGender()))
                .thenReturn(List.of(mission));
            Mockito.when(missionCommandRepository.findByActionTypeAndTargetGender(mission.getActionType(),
                    TargetGender.ALL))
                .thenReturn(List.of());
            Mockito.when(memberCommandRepository.findById(memberId))
                .thenReturn(Optional.of(member));
            Mockito.when(memberMissionCommandRepository.findByMemberIdAndMissionIdOnToday(memberId, missionId))
                .thenReturn(Optional.of(memberMission));

            // When
            boolean hasProcessedMission = memberMissionService.executeMissionsByAction(memberId,
                mission.getActionType().name());

            // Then
            Mockito.verify(memberMissionCommandRepository, Mockito.never()).save(Mockito.any(MemberMission.class));
            assertThat(hasProcessedMission).isTrue();
        }

        @Test
        @DisplayName("멤버가 해당 미션을 완료한 경우, 해당 멤버-미션을 수행하지 않습니다.")
        void notDoMemberMissionWhenIsCompleted() {
            // Given
            MemberMission completedMemberMission = MemberMissionFixture.builder()
                .memberId(memberId)
                .missionId(missionId)
                .successCount(repeatableCount)
                .completed(true)
                .build();
            Mockito.when(missionCommandRepository.findByActionTypeAndTargetGender(mission.getActionType(),
                    mission.getTargetGender()))
                .thenReturn(List.of(mission));
            Mockito.when(missionCommandRepository.findByActionTypeAndTargetGender(mission.getActionType(),
                    TargetGender.ALL))
                .thenReturn(List.of());
            Mockito.when(memberCommandRepository.findById(memberId))
                .thenReturn(Optional.of(member));
            Mockito.when(memberMissionCommandRepository.findByMemberIdAndMissionIdOnToday(memberId, missionId))
                .thenReturn(Optional.of(completedMemberMission));

            // When
            boolean hasProcessedMission = memberMissionService.executeMissionsByAction(memberId,
                mission.getActionType().name());

            // Then
            Mockito.verify(memberMissionCommandRepository, Mockito.never()).save(Mockito.any(MemberMission.class));
            assertThat(completedMemberMission.getSuccessCount()).isEqualTo(repeatableCount);
            assertThat(completedMemberMission.getAttemptCount()).isZero();
            assertThat(hasProcessedMission).isFalse();
        }
    }
}
