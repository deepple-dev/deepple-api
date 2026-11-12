package kr.co.deepple.heart.command.domain.heartransaction.vo;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.heart.command.domain.HeartErrorCode;
import kr.co.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartBalanceTest {

    @Nested
    @DisplayName("init 메서드 테스트")
    class InitTest {
        @Test
        @DisplayName("init 메서드 성공적으로 초기화")
        void initTest() {
            // when
            HeartBalance heartBalance = HeartBalance.init();
            // then
            assertThat(heartBalance).isNotNull();
            assertThat(heartBalance.getPurchaseHeartBalance()).isZero();
            assertThat(heartBalance.getMissionHeartBalance()).isZero();
        }
    }

    @Nested
    @DisplayName("gainPurchaseHeart 메서드 테스트")
    class GainPurchaseHeartTest {
        @Test
        @DisplayName("구매 하트가 수량만큼 늘어난다")
        void increasePurchaseHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            // when
            HeartBalance updatedBalance = heartBalance.gainPurchaseHeart(10L);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(10L);
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -10L})
        @DisplayName("수량이 양수가 아니면 실패한다")
        void failWhenAmountIsNotPositive(long amount) {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            // when & then
            assertThatThrownBy(() -> heartBalance.gainPurchaseHeart(amount))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("gainMissionHeart 메서드 테스트")
    class GainMissionHeartTest {
        @Test
        @DisplayName("미션 하트가 수량만큼 늘어난다")
        void increaseMissionHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            // when
            HeartBalance updatedBalance = heartBalance.gainMissionHeart(10L);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isZero();
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(10L);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -10L})
        @DisplayName("수량이 양수가 아니면 실패한다")
        void failWhenAmountIsNotPositive(long amount) {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            // when & then
            assertThatThrownBy(() -> heartBalance.gainMissionHeart(amount))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("refundPurchaseHeart 메서드 테스트")
    class RefundPurchaseHeartTest {
        @Test
        @DisplayName("구매 하트가 수량만큼 회수된다")
        void reclaimPurchaseHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.of(0L, 100L);
            // when
            HeartBalance updatedBalance = heartBalance.refundPurchaseHeart(50L);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(50L);
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
        }

        @Test
        @DisplayName("회수할 수량이 구매 하트보다 많으면 구매 하트는 0이 된다")
        void clampToZeroWhenAmountExceedsBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.of(0L, 30L);
            // when
            HeartBalance updatedBalance = heartBalance.refundPurchaseHeart(50L);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isZero();
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
        }

        @Test
        @DisplayName("미션 하트는 회수되지 않는다")
        void notAffectMissionHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.of(50L, 100L);
            // when
            HeartBalance updatedBalance = heartBalance.refundPurchaseHeart(30L);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(70L);
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(50L);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -10L})
        @DisplayName("수량이 양수가 아니면 실패한다")
        void failWhenAmountIsNotPositive(long amount) {
            // given
            HeartBalance heartBalance = HeartBalance.of(0L, 100L);
            // when & then
            assertThatThrownBy(() -> heartBalance.refundPurchaseHeart(amount))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("useHeart 메서드 테스트")
    class UseHeartTest {
        @Test
        @DisplayName("수량이 미션 하트보다 적으면 미션 하트만 쓴다")
        void useMissionHeartOnlyWhenAmountIsLessThanMissionHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.of(10L, 10L);
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(1L);
            // then
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(9L);
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(10L);
        }

        @Test
        @DisplayName("수량이 미션 하트와 같으면 미션 하트만 모두 쓴다")
        void useMissionHeartOnlyWhenAmountIsEqualToMissionHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.of(10L, 10L);
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(10L);
            // then
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(10L);
        }

        @Test
        @DisplayName("수량이 미션 하트보다 많으면 미션 하트를 모두 쓰고 나머지를 구매 하트에서 쓴다")
        void usePurchaseHeartForRemainderWhenAmountIsGreaterThanMissionHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.of(10L, 10L);
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(15L);
            // then
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(5L);
        }

        @Test
        @DisplayName("수량이 잔액 총합과 같으면 모두 쓴다")
        void useAllWhenAmountIsEqualToTotalHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.of(10L, 10L);
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(20L);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isZero();
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
        }

        @Test
        @DisplayName("수량이 잔액 총합보다 많으면 잔액 부족으로 실패한다")
        void failWhenAmountIsGreaterThanTotalHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.of(10L, 10L);
            // when & then
            assertThatThrownBy(() -> heartBalance.useHeart(21L))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", HeartErrorCode.INSUFFICIENT_HEART_BALANCE);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        @DisplayName("수량이 양수가 아니면 실패한다")
        void failWhenAmountIsNotPositive(long amount) {
            // given
            HeartBalance heartBalance = HeartBalance.of(10L, 10L);
            // when & then
            assertThatThrownBy(() -> heartBalance.useHeart(amount))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
