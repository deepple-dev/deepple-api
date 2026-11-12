package kr.co.deepple.heart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

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
      assertThat(heartBalance.getPurchaseBalance()).isZero();
      assertThat(heartBalance.getMissionBalance()).isZero();
    }
  }

  @Nested
  @DisplayName("addPurchaseHeart 메서드 테스트")
  class GainPurchaseHeartTest {

    @Test
    @DisplayName("구매 하트가 수량만큼 늘어난다")
    void increasePurchaseHeartBalance() {
      // given
      HeartBalance heartBalance = HeartBalance.init();
      // when
      HeartBalance updatedBalance = heartBalance.apply(TransactionType.PURCHASE, 10L);
      // then
      assertThat(updatedBalance.getPurchaseBalance()).isEqualTo(10L);
      assertThat(updatedBalance.getMissionBalance()).isZero();
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -10L})
    @DisplayName("수량이 양수가 아니면 실패한다")
    void failWhenAmountIsNotPositive(long amount) {
      // given
      HeartBalance heartBalance = HeartBalance.init();
      // when & then
      assertThatThrownBy(() -> heartBalance.apply(TransactionType.PURCHASE, amount))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("addMissionHeart 메서드 테스트")
  class GainMissionHeartTest {

    @Test
    @DisplayName("미션 하트가 수량만큼 늘어난다")
    void increaseMissionHeartBalance() {
      // given
      HeartBalance heartBalance = HeartBalance.init();
      // when
      HeartBalance updatedBalance = heartBalance.apply(TransactionType.MISSION, 10L);
      // then
      assertThat(updatedBalance.getPurchaseBalance()).isZero();
      assertThat(updatedBalance.getMissionBalance()).isEqualTo(10L);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -10L})
    @DisplayName("수량이 양수가 아니면 실패한다")
    void failWhenAmountIsNotPositive(long amount) {
      // given
      HeartBalance heartBalance = HeartBalance.init();
      // when & then
      assertThatThrownBy(() -> heartBalance.apply(TransactionType.MISSION, amount))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("refundHeart 메서드 테스트")
  class RefundHeartTest {

    @Test
    @DisplayName("구매 하트가 수량만큼 회수된다")
    void reclaimPurchaseHeartBalance() {
      // given
      HeartBalance heartBalance = HeartBalance.of(0L, 100L);
      // when
      HeartBalance updatedBalance = heartBalance.apply(TransactionType.REFUND, 50L);
      // then
      assertThat(updatedBalance.getPurchaseBalance()).isEqualTo(50L);
      assertThat(updatedBalance.getMissionBalance()).isZero();
    }

    @Test
    @DisplayName("회수할 수량이 구매 하트보다 많으면 구매 하트는 0이 된다")
    void clampToZeroWhenAmountExceedsBalance() {
      // given
      HeartBalance heartBalance = HeartBalance.of(0L, 30L);
      // when
      HeartBalance updatedBalance = heartBalance.apply(TransactionType.REFUND, 50L);
      // then
      assertThat(updatedBalance.getPurchaseBalance()).isZero();
      assertThat(updatedBalance.getMissionBalance()).isZero();
    }

    @Test
    @DisplayName("미션 하트는 회수되지 않는다")
    void notAffectMissionHeartBalance() {
      // given
      HeartBalance heartBalance = HeartBalance.of(50L, 100L);
      // when
      HeartBalance updatedBalance = heartBalance.apply(TransactionType.REFUND, 30L);
      // then
      assertThat(updatedBalance.getPurchaseBalance()).isEqualTo(70L);
      assertThat(updatedBalance.getMissionBalance()).isEqualTo(50L);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -10L})
    @DisplayName("수량이 양수가 아니면 실패한다")
    void failWhenAmountIsNotPositive(long amount) {
      // given
      HeartBalance heartBalance = HeartBalance.of(0L, 100L);
      // when & then
      assertThatThrownBy(() -> heartBalance.apply(TransactionType.REFUND, amount))
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
      HeartBalance updatedBalance = heartBalance.apply(TransactionType.MESSAGE, 1L);
      // then
      assertThat(updatedBalance.getMissionBalance()).isEqualTo(9L);
      assertThat(updatedBalance.getPurchaseBalance()).isEqualTo(10L);
    }

    @Test
    @DisplayName("수량이 미션 하트와 같으면 미션 하트만 모두 쓴다")
    void useMissionHeartOnlyWhenAmountIsEqualToMissionHeartBalance() {
      // given
      HeartBalance heartBalance = HeartBalance.of(10L, 10L);
      // when
      HeartBalance updatedBalance = heartBalance.apply(TransactionType.MESSAGE, 10L);
      // then
      assertThat(updatedBalance.getMissionBalance()).isZero();
      assertThat(updatedBalance.getPurchaseBalance()).isEqualTo(10L);
    }

    @Test
    @DisplayName("수량이 미션 하트보다 많으면 미션 하트를 모두 쓰고 나머지를 구매 하트에서 쓴다")
    void usePurchaseHeartForRemainderWhenAmountIsGreaterThanMissionHeartBalance() {
      // given
      HeartBalance heartBalance = HeartBalance.of(10L, 10L);
      // when
      HeartBalance updatedBalance = heartBalance.apply(TransactionType.MESSAGE, 15L);
      // then
      assertThat(updatedBalance.getMissionBalance()).isZero();
      assertThat(updatedBalance.getPurchaseBalance()).isEqualTo(5L);
    }

    @Test
    @DisplayName("수량이 잔액 총합과 같으면 모두 쓴다")
    void useAllWhenAmountIsEqualToTotalHeartBalance() {
      // given
      HeartBalance heartBalance = HeartBalance.of(10L, 10L);
      // when
      HeartBalance updatedBalance = heartBalance.apply(TransactionType.MESSAGE, 20L);
      // then
      assertThat(updatedBalance.getPurchaseBalance()).isZero();
      assertThat(updatedBalance.getMissionBalance()).isZero();
    }

    @Test
    @DisplayName("수량이 잔액 총합보다 많으면 잔액 부족으로 실패한다")
    void failWhenAmountIsGreaterThanTotalHeartBalance() {
      // given
      HeartBalance heartBalance = HeartBalance.of(10L, 10L);
      // when & then
      assertThatThrownBy(() -> heartBalance.apply(TransactionType.MESSAGE, 21L))
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
      assertThatThrownBy(() -> heartBalance.apply(TransactionType.MESSAGE, amount))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("거래 종류별 잔액 변경")
  class ChangeTest {

    private final HeartBalance heartBalance = HeartBalance.of(5L, 100L);

    @ParameterizedTest
    @EnumSource(
        value = TransactionType.class,
        names = {"MESSAGE", "DATING_EXAM", "PROFILE_EXCHANGE", "INTRODUCTION"})
    @DisplayName("사용 종류는 미션 하트부터 줄인다.")
    void useForUsingTypes(TransactionType transactionType) {
      assertThat(heartBalance.apply(transactionType, 20L)).isEqualTo(HeartBalance.of(0L, 85L));
    }

    @Test
    @DisplayName("구매는 구매 하트를 늘린다.")
    void addPurchaseHeartForPurchase() {
      assertThat(heartBalance.apply(TransactionType.PURCHASE, 20L))
          .isEqualTo(HeartBalance.of(5L, 120L));
    }

    @ParameterizedTest
    @EnumSource(
        value = TransactionType.class,
        names = {"MISSION", "ADMIN_GRANT"})
    @DisplayName("미션 보상과 관리자 지급은 미션 하트를 늘린다.")
    void addMissionHeartForGrants(TransactionType transactionType) {
      assertThat(heartBalance.apply(transactionType, 20L)).isEqualTo(HeartBalance.of(25L, 100L));
    }

    @Test
    @DisplayName("환불은 구매 하트만 줄인다.")
    void refundHeartForRefund() {
      assertThat(heartBalance.apply(TransactionType.REFUND, 20L))
          .isEqualTo(HeartBalance.of(5L, 80L));
    }
  }
}
