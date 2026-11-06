package deepple.deepple.mission.command.domain.mission;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MissionTest {


    @Nested
    @DisplayName("미션 생성 테스트")
    class Create {

        private static Stream<Arguments> constructorSource() {
            return Stream.of(
                Arguments.of("시도 횟수가 1보다 작은 경우, 예외 발생", 0, 2, 10),
                Arguments.of("반복 횟수가 1보다 작은 경우, 예외 발생", 3, -1, 10),
                Arguments.of("하트 보상이 1보다 작은 경우, 예외 발생", 1, 2, -5));
        }

        @Test
        @DisplayName("들어온 문자의 값이 Enum 으로 변환이 되지 않는 경우, 예외를 발생한다.")
        void throwExceptionWhenStringValueIsNotIncludedEnum() {
            // Given
            String invalidValue = "InvalidValue";

            // When & Then
            Assertions.assertThatThrownBy(() -> ActionType.from(invalidValue))
                .isInstanceOf(IllegalArgumentException.class);

            Assertions.assertThatThrownBy(() -> FrequencyType.from(invalidValue))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("들어온 Enum 값이 null 인 경우, 예외 발생")
        void throwExceptionWhenEnumValueIsNull() {
            // Given
            ActionType actionType = null;
            FrequencyType frequencyType = null;
            TargetGender targetGender = null;

            // When & Then
            Assertions.assertThatThrownBy(() -> Mission.create(actionType, frequencyType, targetGender, 3, 1, 4, true))
                .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("constructorSource")
        void throwExceptionWhenIntValueIsLessThan1(String name, int requiredAttempt, int repeatableCount,
            int rewardedHeart) {
            // Given
            ActionType actionType = ActionType.LIKE;
            FrequencyType frequencyType = FrequencyType.CHALLENGE;
            boolean isPublic = true;

            // When & Then
            Assertions.assertThatThrownBy(
                () -> Mission.create(actionType, frequencyType, TargetGender.MALE, requiredAttempt, repeatableCount,
                    rewardedHeart,
                    isPublic)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("미션을 생성합니다.")
        void createMission() {
            // Given
            ActionType actionType = ActionType.LIKE;
            FrequencyType frequencyType = FrequencyType.CHALLENGE;
            TargetGender targetGender = TargetGender.MALE;
            int requiredAttempt = 10;
            int repeatableCount = 5;
            int rewardedHeart = 6;
            boolean isPublic = true;

            // When
            Mission mission = Mission.create(actionType, frequencyType, targetGender, requiredAttempt, repeatableCount,
                rewardedHeart,
                isPublic);

            // Then
            Assertions.assertThat(mission.getActionType()).isEqualTo(actionType);
            Assertions.assertThat(mission.getFrequencyType()).isEqualTo(frequencyType);
            Assertions.assertThat(mission.getTargetGender()).isEqualTo(targetGender);
            Assertions.assertThat(mission.getRequiredAttempt()).isEqualTo(requiredAttempt);
            Assertions.assertThat(mission.getRepeatableCount()).isEqualTo(repeatableCount);
            Assertions.assertThat(mission.getRewardedHeart()).isEqualTo(rewardedHeart);
            Assertions.assertThat(mission.isPublic()).isEqualTo(isPublic);
        }
    }
}
