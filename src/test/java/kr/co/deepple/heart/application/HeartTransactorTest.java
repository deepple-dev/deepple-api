package kr.co.deepple.heart.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.heart.application.port.out.HeartTransactionRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.domain.HeartBalance;
import kr.co.deepple.heart.domain.HeartErrorCode;
import kr.co.deepple.heart.domain.HeartTransaction;
import kr.co.deepple.heart.domain.MemberHeartBalance;
import kr.co.deepple.heart.domain.TransactionType;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeartTransactorTest {

  private static final long MEMBER_ID = 1L;

  @Mock private MemberHeartBalanceRepository memberHeartBalanceRepository;

  @Mock private HeartTransactionRepository heartTransactionRepository;

  @InjectMocks private HeartTransactor heartTransactor;

  private MemberHeartBalance givenBalance(long missionBalance, long purchaseBalance) {
    MemberHeartBalance balance =
        MemberHeartBalanceFixture.builder()
            .memberId(MEMBER_ID)
            .missionBalance(missionBalance)
            .purchaseBalance(purchaseBalance)
            .build();
    when(memberHeartBalanceRepository.findByMemberIdForUpdate(MEMBER_ID))
        .thenReturn(Optional.of(balance));
    return balance;
  }

  @Test
  @DisplayName("바뀐 잔액과 멱등 키를 담은 거래 내역을 저장하고 잔액에 반영한다.")
  void saveTransactionAndApplyToBalance() {
    // given
    MemberHeartBalance balance = givenBalance(5L, 100L);
    when(heartTransactionRepository.saveIfAbsent(any())).thenReturn(true);

    // when
    boolean transacted =
        heartTransactor.transact(MEMBER_ID, TransactionType.PURCHASE, "하트 구매", 45L, "7");

    // then
    ArgumentCaptor<HeartTransaction> captor = ArgumentCaptor.forClass(HeartTransaction.class);
    verify(heartTransactionRepository).saveIfAbsent(captor.capture());
    HeartTransaction transaction = captor.getValue();
    assertThat(transacted).isTrue();
    assertThat(transaction.getMemberId()).isEqualTo(MEMBER_ID);
    assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.PURCHASE);
    assertThat(transaction.getContent()).isEqualTo("하트 구매");
    assertThat(transaction.getAmount()).isEqualTo(45L);
    assertThat(transaction.getBalance()).isEqualTo(HeartBalance.of(5L, 145L));
    assertThat(transaction.getIdempotencyKey()).isEqualTo("7");
    assertThat(balance.getBalance()).isEqualTo(HeartBalance.of(5L, 145L));
  }

  @Test
  @DisplayName("같은 멱등 키의 거래 내역이 이미 있으면 잔액을 바꾸지 않는다.")
  void skipWhenAlreadyTransacted() {
    // given
    MemberHeartBalance balance = givenBalance(5L, 100L);
    when(heartTransactionRepository.saveIfAbsent(any())).thenReturn(false);

    // when
    boolean transacted =
        heartTransactor.transact(MEMBER_ID, TransactionType.PURCHASE, "하트 구매", 45L, "7");

    // then
    assertThat(transacted).isFalse();
    assertThat(balance.getBalance()).isEqualTo(HeartBalance.of(5L, 100L));
  }

  @Test
  @DisplayName("하트가 모자라면 예외가 발생하고 거래 내역을 저장하지 않는다.")
  void throwWhenInsufficient() {
    // given
    givenBalance(0L, 19L);

    // when & then
    assertThatThrownBy(() ->
            heartTransactor.transact(MEMBER_ID, TransactionType.MESSAGE, "메시지 보내기", 20L, "100"))
        .isInstanceOf(DeeppleException.class)
        .extracting(e -> ((DeeppleException) e).getErrorCode())
        .isEqualTo(HeartErrorCode.INSUFFICIENT_HEART_BALANCE);
    verify(heartTransactionRepository, never()).saveIfAbsent(any());
  }

  @Test
  @DisplayName("잔액 행이 없으면 예외가 발생한다.")
  void throwWhenBalanceMissing() {
    // given
    when(memberHeartBalanceRepository.findByMemberIdForUpdate(MEMBER_ID))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
            () -> heartTransactor.transact(MEMBER_ID, TransactionType.PURCHASE, "하트 구매", 45L, "7"))
        .isInstanceOf(IllegalStateException.class);
  }
}
