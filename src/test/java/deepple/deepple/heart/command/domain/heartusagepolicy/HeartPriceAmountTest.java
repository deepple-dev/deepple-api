package deepple.deepple.heart.command.domain.heartusagepolicy;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartPriceAmountTest {

    @Test
    @DisplayName("price 값이 0이면 IllegalArgumentException 발생")
    void fromTestWhenPriceIsZero() {
        // given
        Long price = 0L;
        // when & then
        assertThatThrownBy(() -> HeartPriceAmount.from(price))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("price 값이 1 이상이면 HeartPriceAmount 객체 생성 성공")
    void fromTestWhenPriceIsPositive() {
        // given
        Long price = 1L;
        // when
        HeartPriceAmount heartPriceAmount = HeartPriceAmount.from(price);
        // then
        assertThat(heartPriceAmount).isNotNull();
    }

    @Test
    @DisplayName("price가 null이면 NullPointerException 발생")
    void throwsNullPointExceptionWhenPriceIsNull() {
        // given
        Long price = null;
        // when & then
        assertThatThrownBy(() -> HeartPriceAmount.from(price))
            .isInstanceOf(NullPointerException.class);
    }
}