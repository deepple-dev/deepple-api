package kr.co.deepple.heart.domain;

import kr.co.deepple.heart.domain.HeartAmount;
import kr.co.deepple.heart.domain.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartAmountTest {

    @Nested
    @DisplayName("of 메서드 테스트")
    class OfTest {
        @Test
        @DisplayName("사용 거래면 음수로 만든다")
        void negateWhenUsingType() {
            // when
            HeartAmount heartAmount = HeartAmount.of(TransactionType.MESSAGE, 20L);
            // then
            assertThat(heartAmount.getAmount()).isEqualTo(-20L);
        }

        @Test
        @DisplayName("획득 거래면 양수 그대로 만든다")
        void keepPositiveWhenGainingType() {
            // when
            HeartAmount heartAmount = HeartAmount.of(TransactionType.PURCHASE, 45L);
            // then
            assertThat(heartAmount.getAmount()).isEqualTo(45L);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -10L})
        @DisplayName("수량이 양수가 아니면 실패한다")
        void failWhenAmountIsNotPositive(long amount) {
            // when & then
            assertThatThrownBy(() -> HeartAmount.of(TransactionType.MESSAGE, amount))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("거래 종류가 null이면 실패한다")
        void failWhenTransactionTypeIsNull() {
            // when & then
            assertThatThrownBy(() -> HeartAmount.of(null, 10L))
                .isInstanceOf(NullPointerException.class);
        }
    }
}
