package kr.co.deepple.heart.command.domain.heartransaction;

import kr.co.deepple.heart.command.domain.hearttransaction.HeartTransaction;
import kr.co.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import kr.co.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartTransactionTest {

    @Nested
    @DisplayName("of 메서드 테스트")
    class OfMethodTest {
        @ParameterizedTest
        @ValueSource(strings = {"memberId is null", "transactionType is null", "heartBalance is null"})
        @DisplayName("of 메서드에서 필드 값이 null이면 예외를 던집니다.")
        void ofMethodWithNullFieldThrowsException(String fieldName) {
            // given
            Long memberId = fieldName.equals("memberId is null") ? null : 1L;
            TransactionType transactionType =
                fieldName.equals("transactionType is null") ? null : TransactionType.MISSION;
            String content = TransactionType.MISSION.getDescription();
            HeartBalance heartBalance = fieldName.equals("heartBalance is null") ? null : HeartBalance.init();

            // when & then
            assertThatThrownBy(
                () -> HeartTransaction.of(memberId, transactionType, content, 10L, heartBalance))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("사용 거래는 수량을 음수로 기록한다")
        void recordNegativeAmountForUsingType() {
            // given
            TransactionType transactionType = TransactionType.MESSAGE;

            // when
            HeartTransaction heartTransaction = HeartTransaction.of(1L, transactionType,
                transactionType.getDescription(), 10L, HeartBalance.init());

            // then
            assertThat(heartTransaction.getHeartAmount().getAmount()).isEqualTo(-10L);
        }

        @Test
        @DisplayName("획득 거래는 수량을 양수로 기록한다")
        void recordPositiveAmountForGainingType() {
            // given
            TransactionType transactionType = TransactionType.MISSION;

            // when
            HeartTransaction heartTransaction = HeartTransaction.of(1L, transactionType,
                transactionType.getDescription(), 10L, HeartBalance.init());

            // then
            assertThat(heartTransaction.getHeartAmount().getAmount()).isEqualTo(10L);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -10L})
        @DisplayName("수량이 양수가 아니면 실패한다")
        void failWhenAmountIsNotPositive(long amount) {
            // given
            TransactionType transactionType = TransactionType.MESSAGE;

            // when & then
            assertThatThrownBy(() -> HeartTransaction.of(1L, transactionType, transactionType.getDescription(),
                amount, HeartBalance.init()))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
