package deepple.deepple.heart.command.domain.heartransaction.vo;

import deepple.deepple.heart.command.domain.hearttransaction.exception.InsufficientHeartBalanceException;
import deepple.deepple.heart.command.domain.hearttransaction.exception.InvalidHeartAmountException;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartAmount;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
        @DisplayName("amount 값 양수로 purchaseHeartBalance 증가 성공")
        void gainPurchaseHeartTestWithPositiveValue() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            HeartAmount heartAmount = HeartAmount.from(10L);
            // when
            HeartBalance updatedBalance = heartBalance.gainPurchaseHeart(heartAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(10L);
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
        }

        @Test
        @DisplayName("amount 값 음수로 purchaseHeartBalance 증가 실패")
        void gainPurchaseHeartTestWithNegativeValue() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            HeartAmount heartAmount = HeartAmount.from(-10L);
            // when
            // then
            assertThatThrownBy(() -> heartBalance.gainPurchaseHeart(heartAmount))
                .isInstanceOf(InvalidHeartAmountException.class);
        }

        @Test
        @DisplayName("amount 값 0으로 purchaseHeartBalance 증가 실패")
        void gainPurchaseHeartTestWithZeroValue() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            HeartAmount heartAmount = HeartAmount.from(0L);
            // when
            // then
            assertThatThrownBy(() -> heartBalance.gainPurchaseHeart(heartAmount))
                .isInstanceOf(InvalidHeartAmountException.class);
        }
    }

    @Nested
    @DisplayName("gainMissionHeart 메서드 테스트")
    class GainMissionHeartTest {
        @Test
        @DisplayName("amount 값 양수로 missionHeartBalance 증가 성공")
        void gainMissionHeartTestWithPositiveValue() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            HeartAmount heartAmount = HeartAmount.from(10L);
            // when
            HeartBalance updatedBalance = heartBalance.gainMissionHeart(heartAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isZero();
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(10L);
        }

        @Test
        @DisplayName("amount 값 음수로 missionHeartBalance 증가 실패")
        void gainMissionHeartTestWithNegativeValue() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            HeartAmount heartAmount = HeartAmount.from(-10L);
            // when
            // then
            assertThatThrownBy(() -> heartBalance.gainMissionHeart(heartAmount))
                .isInstanceOf(InvalidHeartAmountException.class);
        }

        @Test
        @DisplayName("amount 값 0으로 missionHeartBalance 증가 실패")
        void gainMissionHeartTestWithZeroValue() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            HeartAmount heartAmount = HeartAmount.from(0L);
            // when
            // then
            assertThatThrownBy(() -> heartBalance.gainMissionHeart(heartAmount))
                .isInstanceOf(InvalidHeartAmountException.class);
        }
    }

    @Nested
    @DisplayName("refundPurchaseHeart 메서드 테스트")
    class RefundPurchaseHeartTest {
        @Test
        @DisplayName("amount 값 음수로 purchaseHeartBalance 환불 성공")
        void refundPurchaseHeartTestWithNegativeValue() {
            // given
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(100L));
            HeartAmount refundAmount = HeartAmount.from(-50L);
            // when
            HeartBalance updatedBalance = heartBalance.refundPurchaseHeart(refundAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(50L);
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
        }

        @Test
        @DisplayName("amount 값 음수이고 purchaseHeartBalance보다 큰 경우 0으로 제한")
        void refundPurchaseHeartTestWithNegativeValueGreaterThanBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(30L));
            HeartAmount refundAmount = HeartAmount.from(-50L);
            // when
            HeartBalance updatedBalance = heartBalance.refundPurchaseHeart(refundAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isZero();
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
        }

        @Test
        @DisplayName("purchaseHeartBalance가 0일 때 음수 환불 시도하면 0 유지")
        void refundPurchaseHeartTestWithZeroBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            HeartAmount refundAmount = HeartAmount.from(-10L);
            // when
            HeartBalance updatedBalance = heartBalance.refundPurchaseHeart(refundAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isZero();
            assertThat(updatedBalance.getMissionHeartBalance()).isZero();
        }

        @Test
        @DisplayName("amount 값 양수로 purchaseHeartBalance 환불 실패")
        void refundPurchaseHeartTestWithPositiveValue() {
            // given
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(100L));
            HeartAmount refundAmount = HeartAmount.from(10L);
            // when & then
            assertThatThrownBy(() -> heartBalance.refundPurchaseHeart(refundAmount))
                .isInstanceOf(InvalidHeartAmountException.class);
        }

        @Test
        @DisplayName("amount 값 0으로 purchaseHeartBalance 환불 실패")
        void refundPurchaseHeartTestWithZeroValue() {
            // given
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(100L));
            HeartAmount refundAmount = HeartAmount.from(0L);
            // when & then
            assertThatThrownBy(() -> heartBalance.refundPurchaseHeart(refundAmount))
                .isInstanceOf(InvalidHeartAmountException.class);
        }

        @Test
        @DisplayName("missionHeartBalance는 환불에 영향을 받지 않음")
        void refundPurchaseHeartTestDoesNotAffectMissionHeartBalance() {
            // given
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(100L))
                .gainMissionHeart(HeartAmount.from(50L));
            HeartAmount refundAmount = HeartAmount.from(-30L);
            // when
            HeartBalance updatedBalance = heartBalance.refundPurchaseHeart(refundAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(70L);
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("useHeart 메서드 테스트")
    class UseHeartTest {
        @Test
        @DisplayName("가격이 purchaseHaertBalance 보다 작은 경우 purchaseHeartBalance 만 사용 성공")
        void shouldSucceedWhenAmountIsLessThanPurchaseHeartBalance() {
            // given
            Long purchaseHeartBalance = 10L;
            Long missionHeartBalance = 10L;
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(purchaseHeartBalance))
                .gainMissionHeart(HeartAmount.from(missionHeartBalance));
            HeartAmount heartAmount = HeartAmount.from(-1L);
            Long expectedPurchaseHeartBalance = purchaseHeartBalance + heartAmount.getAmount();
            Long expectedMissionHeartBalance = missionHeartBalance;
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(heartAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(expectedPurchaseHeartBalance);
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(expectedMissionHeartBalance);
        }

        @Test
        @DisplayName("가격이 purchaseHaertBalance 와 같은 경우 purchaseHeartBalance 만 사용 성공")
        void shouldSucceedWhenAmountIsEqualToPurchaseHeartBalance() {
            // given
            Long purchaseHeartBalance = 10L;
            Long missionHeartBalance = 10L;
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(purchaseHeartBalance))
                .gainMissionHeart(HeartAmount.from(missionHeartBalance));
            HeartAmount heartAmount = HeartAmount.from(-10L);
            Long expectedPurchaseHeartBalance = purchaseHeartBalance + heartAmount.getAmount();
            Long expectedMissionHeartBalance = missionHeartBalance;
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(heartAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(expectedPurchaseHeartBalance);
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(expectedMissionHeartBalance);
        }

        @Test
        @DisplayName("가격이 purchaseHaertBalance 보다 큰 경우 purchaseHeartBalance를 모두 사용하고 missionHeartBalance 도 사용 성공")
        void shouldSucceedWhenAmountIsGreaterThanPurchaseHeartBalance() {
            // given
            Long purchaseHeartBalance = 10L;
            Long missionHeartBalance = 10L;
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(purchaseHeartBalance))
                .gainMissionHeart(HeartAmount.from(missionHeartBalance));
            HeartAmount heartAmount = HeartAmount.from(-15L);
            Long expectedPurchaseHeartBalance = 0L;
            Long expectedMissionHeartBalance = missionHeartBalance + (purchaseHeartBalance + heartAmount.getAmount());
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(heartAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(expectedPurchaseHeartBalance);
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(expectedMissionHeartBalance);
        }

        @Test
        @DisplayName("가격이 haertBalance 총합과 같은 경우 purchaseHeartBalance와 missionHeartBalance를 모두 사용 성공")
        void shouldSucceedWhenAmountIsEqualToTotalHeartBalance() {
            // given
            Long purchaseHeartBalance = 10L;
            Long missionHeartBalance = 10L;
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(purchaseHeartBalance))
                .gainMissionHeart(HeartAmount.from(missionHeartBalance));
            HeartAmount heartAmount = HeartAmount.from(-20L);
            Long expectedPurchaseHeartBalance = 0L;
            Long expectedMissionHeartBalance = 0L;
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(heartAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(expectedPurchaseHeartBalance);
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(expectedMissionHeartBalance);
        }

        @Test
        @DisplayName("가격이 haertBalance 총합보다 큰 경우 사용 실패")
        void shouldFailWhenAmountIsGreaterThanTotalHeartBalance() {
            // given
            Long purchaseHeartBalance = 10L;
            Long missionHeartBalance = 10L;
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(purchaseHeartBalance))
                .gainMissionHeart(HeartAmount.from(missionHeartBalance));
            HeartAmount heartAmount = HeartAmount.from(-21L);
            // when
            // then
            assertThatThrownBy(() -> heartBalance.useHeart(heartAmount))
                .isInstanceOf(InsufficientHeartBalanceException.class);
        }

        @Test
        @DisplayName("가격이 0인 경우 사용 성공")
        void shouldFailWhenAmountIsZero() {
            // given
            Long purchaseHeartBalance = 10L;
            Long missionHeartBalance = 10L;
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(purchaseHeartBalance))
                .gainMissionHeart(HeartAmount.from(missionHeartBalance));
            HeartAmount heartAmount = HeartAmount.from(0L);
            Long expectedPurchaseHeartBalance = purchaseHeartBalance;
            Long expectedMissionHeartBalance = missionHeartBalance;
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(heartAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(expectedPurchaseHeartBalance);
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(expectedMissionHeartBalance);
        }

        @Test
        @DisplayName("가격이 0이고 HeartBalance 총합이 0인 경우 사용 성공")
        void shouldFailWhenAmountIsZeroAndHeartBalanceIsZero() {
            // given
            HeartBalance heartBalance = HeartBalance.init();
            HeartAmount heartAmount = HeartAmount.from(0L);
            Long expectedPurchaseHeartBalance = 0L;
            Long expectedMissionHeartBalance = 0L;
            // when
            HeartBalance updatedBalance = heartBalance.useHeart(heartAmount);
            // then
            assertThat(updatedBalance.getPurchaseHeartBalance()).isEqualTo(expectedPurchaseHeartBalance);
            assertThat(updatedBalance.getMissionHeartBalance()).isEqualTo(expectedMissionHeartBalance);
        }

        @Test
        @DisplayName("가격이 양수인 경우 사용 실패")
        void shouldFailWhenAmountIsPositive() {
            // given
            Long purchaseHeartBalance = 10L;
            Long missionHeartBalance = 10L;
            HeartBalance heartBalance = HeartBalance.init()
                .gainPurchaseHeart(HeartAmount.from(purchaseHeartBalance))
                .gainMissionHeart(HeartAmount.from(missionHeartBalance));
            HeartAmount heartAmount = HeartAmount.from(1L);
            // when
            // then
            assertThatThrownBy(() -> heartBalance.useHeart(heartAmount))
                .isInstanceOf(InvalidHeartAmountException.class);
        }
    }
}
