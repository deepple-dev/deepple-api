package deepple.deepple.heart.command.domain.heartusagepolicy;

import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import deepple.deepple.member.command.domain.member.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartUsagePolicyTest {

    @Nested
    @DisplayName("of 메서드 테스트")
    class OfTest {

        @Test
        @DisplayName("HeartUsagePolicy 객체를 생성하는 테스트")
        void ofTest() {
            // given
            TransactionType transactionType = TransactionType.MESSAGE;
            Gender gender = Gender.MALE;
            HeartPriceAmount heartPriceAmount = HeartPriceAmount.from(10L);
            // when
            HeartUsagePolicy heartUsagePolicy = HeartUsagePolicy.of(transactionType, gender, heartPriceAmount);
            // then
            assertThat(heartUsagePolicy).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"usageType is null", "gender is null", "heartPriceAmount is null"})
        @DisplayName("of 메서드에서 필드 값이 null이면 예외를 던집니다.")
        void ofMethodWithNullFieldThrowsException(String fieldName) {
            // given
            TransactionType transactionType = fieldName.equals("usageType is null") ? null : TransactionType.MESSAGE;
            Gender gender = fieldName.equals("gender is null") ? null : Gender.MALE;
            HeartPriceAmount heartPriceAmount =
                fieldName.equals("heartPriceAmount is null") ? null : HeartPriceAmount.from(10L);
            // when & then
            assertThatThrownBy(() -> HeartUsagePolicy.of(transactionType, gender, heartPriceAmount))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("transaction type이 using type이 아닌 경우 예외 발생")
        void ofMethodWithNotUsingTransactionTypeThrowsException() {
            // given
            TransactionType transactionType = TransactionType.MISSION;
            Gender gender = Gender.MALE;
            HeartPriceAmount heartPriceAmount = HeartPriceAmount.from(10L);
            // when & then
            assertThatThrownBy(() -> HeartUsagePolicy.of(transactionType, gender, heartPriceAmount))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getTransactionType 메서드 테스트")
    class GetTransactionTypeTest {

        @Test
        @DisplayName("HeartUsagePolicy 객체의 TransactionType을 반환하는 테스트")
        void getTransactionTypeTest() {
            // given
            TransactionType transactionType = TransactionType.MESSAGE;
            Gender gender = Gender.MALE;
            HeartPriceAmount heartPriceAmount = HeartPriceAmount.from(10L);
            HeartUsagePolicy heartUsagePolicy = HeartUsagePolicy.of(transactionType, gender, heartPriceAmount);
            // when
            TransactionType returnedTransactionType = heartUsagePolicy.getTransactionType();
            // then
            assertThat(transactionType).isEqualTo(returnedTransactionType);
        }
    }
}