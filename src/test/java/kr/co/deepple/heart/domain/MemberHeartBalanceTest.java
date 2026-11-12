package kr.co.deepple.heart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberHeartBalanceTest {

  @Test
  @DisplayName("거래 내역을 적용하면 잔액이 내역의 잔액으로 바뀐다.")
  void applyTransaction() {
    // given
    MemberHeartBalance balance =
        MemberHeartBalanceFixture.builder()
            .memberId(1L)
            .purchaseBalance(100L)
            .missionBalance(5L)
            .build();
    HeartTransaction transaction =
        HeartTransaction.of(
            1L, TransactionType.PURCHASE, "하트 구매", 45L, HeartBalance.of(5L, 145L), "7");

    // when
    balance.apply(transaction);

    // then
    assertThat(balance.getBalance()).isEqualTo(HeartBalance.of(5L, 145L));
  }

  @Test
  @DisplayName("다른 회원의 거래 내역을 적용하면 예외가 발생한다.")
  void throwWhenApplyingOtherMembersTransaction() {
    // given
    MemberHeartBalance balance = MemberHeartBalanceFixture.builder().memberId(1L).build();
    HeartTransaction transaction =
        HeartTransaction.of(
            2L, TransactionType.PURCHASE, "하트 구매", 45L, HeartBalance.of(0L, 45L), "7");

    // when & then
    assertThatThrownBy(() -> balance.apply(transaction))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
