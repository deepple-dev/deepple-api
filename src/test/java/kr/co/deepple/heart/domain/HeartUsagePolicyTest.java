package kr.co.deepple.heart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class HeartUsagePolicyTest {

  @Nested
  @DisplayName("of 메서드 테스트")
  class OfTest {

    @Test
    @DisplayName("받은 값으로 HeartUsagePolicy를 만든다")
    void ofTest() {
      // when
      HeartUsagePolicy heartUsagePolicy =
          HeartUsagePolicy.of(TransactionType.MESSAGE, Gender.MALE, 1L);
      // then
      assertThat(heartUsagePolicy.getTransactionType()).isEqualTo(TransactionType.MESSAGE);
      assertThat(heartUsagePolicy.getGender()).isEqualTo(Gender.MALE);
      assertThat(heartUsagePolicy.getPrice()).isEqualTo(1L);
    }

    @Test
    @DisplayName("transactionType이 null이면 예외를 던진다")
    void throwsExceptionWhenTransactionTypeIsNull() {
      assertThatThrownBy(() -> HeartUsagePolicy.of(null, Gender.MALE, 10L))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("gender가 null이면 예외를 던진다")
    void throwsExceptionWhenGenderIsNull() {
      assertThatThrownBy(() -> HeartUsagePolicy.of(TransactionType.MESSAGE, null, 10L))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("transaction type이 using type이 아닌 경우 예외 발생")
    void ofMethodWithNotUsingTransactionTypeThrowsException() {
      assertThatThrownBy(() -> HeartUsagePolicy.of(TransactionType.MISSION, Gender.MALE, 10L))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("price 값이 0이면 IllegalArgumentException 발생")
    void throwsExceptionWhenPriceIsZero() {
      assertThatThrownBy(() -> HeartUsagePolicy.of(TransactionType.MESSAGE, Gender.MALE, 0L))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
