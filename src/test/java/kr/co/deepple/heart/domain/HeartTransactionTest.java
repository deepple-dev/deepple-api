package kr.co.deepple.heart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HeartTransactionTest {

  private static final HeartBalance BALANCE = HeartBalance.of(5L, 100L);

  @Test
  @DisplayName("받은 값을 그대로 담는다.")
  void create() {
    // when
    HeartTransaction transaction =
        HeartTransaction.of(1L, TransactionType.PURCHASE, "하트 구매", 45L, BALANCE, "7");

    // then
    assertThat(transaction.getMemberId()).isEqualTo(1L);
    assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.PURCHASE);
    assertThat(transaction.getContent()).isEqualTo("하트 구매");
    assertThat(transaction.getBalance()).isEqualTo(BALANCE);
    assertThat(transaction.getIdempotencyKey()).isEqualTo("7");
  }

  @Test
  @DisplayName("사용 거래는 수량을 음수로 기록한다.")
  void recordNegativeAmountForSpendingType() {
    // when
    HeartTransaction transaction =
        HeartTransaction.of(1L, TransactionType.MESSAGE, "메시지 보내기", 10L, BALANCE, null);

    // then
    assertThat(transaction.getAmount()).isEqualTo(-10L);
  }

  @Test
  @DisplayName("지급 거래는 수량을 양수로 기록한다.")
  void recordPositiveAmountForGrantingType() {
    // when
    HeartTransaction transaction =
        HeartTransaction.of(1L, TransactionType.MISSION, "미션 완료", 10L, BALANCE, null);

    // then
    assertThat(transaction.getAmount()).isEqualTo(10L);
  }

  @ParameterizedTest
  @ValueSource(strings = {"transactionType is null", "content is null", "balance is null"})
  @DisplayName("거래 종류, 내용, 잔액이 null이면 예외가 발생한다.")
  void throwWhenFieldIsNull(String fieldName) {
    // given
    TransactionType transactionType =
        fieldName.equals("transactionType is null") ? null : TransactionType.MISSION;
    String content = fieldName.equals("content is null") ? null : "미션 완료";
    HeartBalance balance = fieldName.equals("balance is null") ? null : BALANCE;

    // when & then
    assertThatThrownBy(() -> HeartTransaction.of(1L, transactionType, content, 10L, balance, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("내용이 비어 있으면 예외가 발생한다.")
  void throwWhenContentIsBlank() {
    assertThatThrownBy(
            () -> HeartTransaction.of(1L, TransactionType.MISSION, " ", 10L, BALANCE, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, -10L})
  @DisplayName("수량이 양수가 아니면 예외가 발생한다.")
  void throwWhenAmountIsNotPositive(long amount) {
    assertThatThrownBy(
            () -> HeartTransaction.of(1L, TransactionType.MISSION, "미션 완료", amount, BALANCE, null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
