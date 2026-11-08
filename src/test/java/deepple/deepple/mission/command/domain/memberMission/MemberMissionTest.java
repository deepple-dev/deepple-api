package deepple.deepple.mission.command.domain.memberMission;

import deepple.deepple.mission.fixture.MemberMissionFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class MemberMissionTest {
    @Nested
    @DisplayName("멤버 미션 생성 테스트")
    class Create {

        @DisplayName("멤버 미션을 생성합니다.")
        @Test
        void createMemberMission() {
            // Given
            long memberId = 1L;
            long missionId = 2L;
            int attemptCount = 0;
            int successCount = 0;
            boolean isCompleted = false;

            // When
            MemberMission memberMission = MemberMission.create(memberId, missionId);

            // Then
            Assertions.assertThat(memberMission.getMemberId()).isEqualTo(memberId);
            Assertions.assertThat(memberMission.getMissionId()).isEqualTo(missionId);
            Assertions.assertThat(memberMission.getAttemptCount()).isEqualTo(attemptCount);
            Assertions.assertThat(memberMission.getSuccessCount()).isEqualTo(successCount);
            Assertions.assertThat(memberMission.isCompleted()).isEqualTo(isCompleted);
        }
    }

    @Nested
    @DisplayName("멤버 미션 수행 테스트")
    class Do {

        @DisplayName("미션을 수행합니다.")
        @Test
        void doMission() {
            // Given
            int requiredAttempt = 3;
            int repeatableCount = 2;
            long missionId = 1L;
            long memberId = 2L;
            MemberMission memberMission = MemberMissionFixture.builder()
                .memberId(memberId)
                .missionId(missionId)
                .build();

            // When
            memberMission.countPlus(requiredAttempt, repeatableCount);

            // Then
            Assertions.assertThat(memberMission.getSuccessCount()).isEqualTo(0);
            Assertions.assertThat(memberMission.getAttemptCount()).isEqualTo(1);
            Assertions.assertThat(memberMission.isCompleted()).isFalse();
        }

        @DisplayName("미션을 완료합니다.")
        @Test
        void completeMission() {
            // Given
            int requiredAttempt = 1;
            int repeatableCount = 1;
            long missionId = 1L;
            long memberId = 2L;
            MemberMission memberMission = MemberMissionFixture.builder()
                .memberId(memberId)
                .missionId(missionId)
                .build();

            // When
            memberMission.countPlus(requiredAttempt, repeatableCount);

            // Then
            Assertions.assertThat(memberMission.getSuccessCount()).isEqualTo(1);
            Assertions.assertThat(memberMission.getAttemptCount()).isEqualTo(0);
            Assertions.assertThat(memberMission.isCompleted()).isTrue();
        }
    }
}
