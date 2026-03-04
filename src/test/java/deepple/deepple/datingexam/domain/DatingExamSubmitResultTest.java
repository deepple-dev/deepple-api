package deepple.deepple.datingexam.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DatingExamSubmitResultTest {

    @Nested
    @DisplayName("create 메서드 테스트")
    class CreateTests {

        @Test
        @DisplayName("초기 생성 시 모든 카운트가 0이고 dominant는 DECISIVE_INDEPENDENT이다.")
        void createWithInitialValues() {
            // When
            DatingExamSubmitResult result = DatingExamSubmitResult.create(1L);

            // Then
            assertThat(result.getMemberId()).isEqualTo(1L);
            assertThat(result.getDecisiveIndependentCount()).isZero();
            assertThat(result.getGrowingRunningMateCount()).isZero();
            assertThat(result.getDevotedRomanticCount()).isZero();
            assertThat(result.getRealisticShelterCount()).isZero();
            assertThat(result.getDominantPersonalityType()).isEqualTo(AnswerPersonalityType.DECISIVE_INDEPENDENT);
        }
    }

    @Nested
    @DisplayName("addCounts 메서드 테스트")
    class AddCountsTests {

        @Test
        @DisplayName("카운트를 추가하면 해당 타입의 카운트가 증가하고 dominant가 재계산된다.")
        void addCountsUpdatesDominant() {
            // Given
            DatingExamSubmitResult result = DatingExamSubmitResult.create(1L);

            // When
            result.addCounts(Map.of(
                AnswerPersonalityType.DEVOTED_ROMANTIC, 3,
                AnswerPersonalityType.DECISIVE_INDEPENDENT, 1
            ));

            // Then
            assertThat(result.getDecisiveIndependentCount()).isEqualTo(1);
            assertThat(result.getDevotedRomanticCount()).isEqualTo(3);
            assertThat(result.getDominantPersonalityType()).isEqualTo(AnswerPersonalityType.DEVOTED_ROMANTIC);
        }

        @Test
        @DisplayName("여러 번 addCounts를 호출하면 카운트가 누적된다.")
        void addCountsAccumulates() {
            // Given
            DatingExamSubmitResult result = DatingExamSubmitResult.create(1L);

            // When
            result.addCounts(Map.of(AnswerPersonalityType.GROWING_RUNNING_MATE, 2));
            result.addCounts(Map.of(AnswerPersonalityType.GROWING_RUNNING_MATE, 3));

            // Then
            assertThat(result.getGrowingRunningMateCount()).isEqualTo(5);
            assertThat(result.getDominantPersonalityType()).isEqualTo(AnswerPersonalityType.GROWING_RUNNING_MATE);
        }

        @Test
        @DisplayName("동점 시 ordinal이 작은 타입이 dominant가 된다 (A>B>C>D 우선순위).")
        void tieBreaksToSmallerOrdinal() {
            // Given
            DatingExamSubmitResult result = DatingExamSubmitResult.create(1L);

            // When
            result.addCounts(Map.of(
                AnswerPersonalityType.DECISIVE_INDEPENDENT, 2,
                AnswerPersonalityType.GROWING_RUNNING_MATE, 2,
                AnswerPersonalityType.DEVOTED_ROMANTIC, 2,
                AnswerPersonalityType.REALISTIC_SHELTER, 2
            ));

            // Then
            assertThat(result.getDominantPersonalityType()).isEqualTo(AnswerPersonalityType.DECISIVE_INDEPENDENT);
        }

        @Test
        @DisplayName("B와 C가 동점이면 B가 우선한다.")
        void tieBreakBOverC() {
            // Given
            DatingExamSubmitResult result = DatingExamSubmitResult.create(1L);

            // When
            result.addCounts(Map.of(
                AnswerPersonalityType.GROWING_RUNNING_MATE, 3,
                AnswerPersonalityType.DEVOTED_ROMANTIC, 3
            ));

            // Then
            assertThat(result.getDominantPersonalityType()).isEqualTo(AnswerPersonalityType.GROWING_RUNNING_MATE);
        }

        @Test
        @DisplayName("빈 맵으로 addCounts를 호출해도 정상 동작한다.")
        void addCountsWithEmptyMap() {
            // Given
            DatingExamSubmitResult result = DatingExamSubmitResult.create(1L);

            // When
            result.addCounts(Map.of());

            // Then
            assertThat(result.getDecisiveIndependentCount()).isZero();
            assertThat(result.getDominantPersonalityType()).isEqualTo(AnswerPersonalityType.DECISIVE_INDEPENDENT);
        }
    }
}
