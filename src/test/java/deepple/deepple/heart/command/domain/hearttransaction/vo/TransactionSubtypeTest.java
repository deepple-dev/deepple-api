package deepple.deepple.heart.command.domain.hearttransaction.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionSubtypeTest {

    @Nested
    @DisplayName("isFreeFor 메서드는")
    class IsFreeEligible {
        @Test
        @DisplayName("INTRODUCTION 타입에 대해 TODAY_CARD 또는 SOULMATE인 경우 true를 반환한다")
        void testIntroductionFreeEligible() {
            // given
            TransactionType transactionType = TransactionType.INTRODUCTION;
            TransactionSubtype subtypeTodayCard = TransactionSubtype.TODAY_CARD;
            TransactionSubtype subtypeSoulmate = TransactionSubtype.SOULMATE;

            // when
            boolean resultTodayCard = subtypeTodayCard.isFreeFor(transactionType);
            boolean resultSoulmate = subtypeSoulmate.isFreeFor(transactionType);

            // then
            assertThat(resultTodayCard).isTrue();
            assertThat(resultSoulmate).isTrue();
        }

        @Test
        @DisplayName("MESSAGE 타입에 대해 SOULMATE인 경우 true를 반환한다")
        void testMessageFreeEligible() {
            // given
            TransactionType transactionType = TransactionType.MESSAGE;
            TransactionSubtype subtypeSoulmate = TransactionSubtype.SOULMATE;

            // when
            boolean resultSoulmate = subtypeSoulmate.isFreeFor(transactionType);

            // then
            assertThat(resultSoulmate).isTrue();
        }

        @Test
        @DisplayName("그 외의 경우 false를 반환한다")
        void testNotFreeEligible() {
            // given
            TransactionType transactionTypeIntroduction = TransactionType.INTRODUCTION;
            TransactionType transactionTypeMessage = TransactionType.MESSAGE;

            TransactionSubtype subtypeNonFree1 = TransactionSubtype.DIAMOND_GRADE;
            TransactionSubtype subtypeNonFree2 = TransactionSubtype.SAME_HOBBY;
            TransactionSubtype subtypeNonFree3 = TransactionSubtype.MATCH;

            // when
            boolean result1 = subtypeNonFree1.isFreeFor(transactionTypeIntroduction);
            boolean result2 = subtypeNonFree2.isFreeFor(transactionTypeMessage);
            boolean result3 = subtypeNonFree3.isFreeFor(transactionTypeMessage);

            // then
            assertThat(result1).isFalse();
            assertThat(result2).isFalse();
            assertThat(result3).isFalse();
        }
    }

}
