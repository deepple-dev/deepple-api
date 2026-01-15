package deepple.deepple.payment.command.domain.refund;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundDetailTest {

    @Nested
    @DisplayName("RefundDetail 객체 생성 테스트")
    class CreateRefundDetailTest {

        @Test
        @DisplayName("유효한 파라미터로 RefundDetail 객체 생성 성공")
        void createRefundDetailSuccessWhenParametersAreValid() {
            // given
            String productId = "product123";
            Integer quantity = 2;
            Long refundAmount = 2000L;

            // when
            RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);

            // then
            assertThat(refundDetail).isNotNull();
            assertThat(refundDetail.getProductId()).isEqualTo(productId);
            assertThat(refundDetail.getQuantity()).isEqualTo(quantity);
            assertThat(refundDetail.getRefundAmount()).isEqualTo(refundAmount);
        }

        @Test
        @DisplayName("refundAmount가 0일 때 객체 생성 성공")
        void createRefundDetailSuccessWhenRefundAmountIsZero() {
            // given
            String productId = "product123";
            Integer quantity = 1;
            Long refundAmount = 0L;

            // when
            RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);

            // then
            assertThat(refundDetail).isNotNull();
            assertThat(refundDetail.getRefundAmount()).isZero();
        }
    }

    @Nested
    @DisplayName("productId 검증 테스트")
    class ProductIdValidationTest {

        @Test
        @DisplayName("productId가 null이면 IllegalArgumentException 발생")
        void throwsIllegalArgumentExceptionWhenProductIdIsNull() {
            // given
            Integer quantity = 1;
            Long refundAmount = 1000L;

            // when & then
            assertThatThrownBy(() -> RefundDetail.of(null, quantity, refundAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId는 null이거나 빈 문자열일 수 없습니다.");
        }

        @Test
        @DisplayName("productId가 빈 문자열이면 IllegalArgumentException 발생")
        void throwsIllegalArgumentExceptionWhenProductIdIsEmpty() {
            // given
            String productId = "";
            Integer quantity = 1;
            Long refundAmount = 1000L;

            // when & then
            assertThatThrownBy(() -> RefundDetail.of(productId, quantity, refundAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId는 null이거나 빈 문자열일 수 없습니다.");
        }

        @Test
        @DisplayName("productId가 공백 문자열이면 IllegalArgumentException 발생")
        void throwsIllegalArgumentExceptionWhenProductIdIsBlank() {
            // given
            String productId = "   ";
            Integer quantity = 1;
            Long refundAmount = 1000L;

            // when & then
            assertThatThrownBy(() -> RefundDetail.of(productId, quantity, refundAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId는 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("quantity 검증 테스트")
    class QuantityValidationTest {

        @Test
        @DisplayName("quantity가 null이면 IllegalArgumentException 발생")
        void throwsIllegalArgumentExceptionWhenQuantityIsNull() {
            // given
            String productId = "product123";
            Long refundAmount = 1000L;

            // when & then
            assertThatThrownBy(() -> RefundDetail.of(productId, null, refundAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity는 0보다 커야 합니다.");
        }

        @Test
        @DisplayName("quantity가 0이면 IllegalArgumentException 발생")
        void throwsIllegalArgumentExceptionWhenQuantityIsZero() {
            // given
            String productId = "product123";
            Integer quantity = 0;
            Long refundAmount = 1000L;

            // when & then
            assertThatThrownBy(() -> RefundDetail.of(productId, quantity, refundAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity는 0보다 커야 합니다.");
        }

        @Test
        @DisplayName("quantity가 음수이면 IllegalArgumentException 발생")
        void throwsIllegalArgumentExceptionWhenQuantityIsNegative() {
            // given
            String productId = "product123";
            Integer quantity = -1;
            Long refundAmount = 1000L;

            // when & then
            assertThatThrownBy(() -> RefundDetail.of(productId, quantity, refundAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity는 0보다 커야 합니다.");
        }

        @Test
        @DisplayName("quantity가 1이면 객체 생성 성공")
        void createRefundDetailSuccessWhenQuantityIsOne() {
            // given
            String productId = "product123";
            Integer quantity = 1;
            Long refundAmount = 1000L;

            // when
            RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);

            // then
            assertThat(refundDetail).isNotNull();
            assertThat(refundDetail.getQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("quantity가 큰 양수이면 객체 생성 성공")
        void createRefundDetailSuccessWhenQuantityIsLarge() {
            // given
            String productId = "product123";
            Integer quantity = 100;
            Long refundAmount = 100000L;

            // when
            RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);

            // then
            assertThat(refundDetail).isNotNull();
            assertThat(refundDetail.getQuantity()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("refundAmount 검증 테스트")
    class RefundAmountValidationTest {

        @Test
        @DisplayName("refundAmount가 null이면 IllegalArgumentException 발생")
        void throwsIllegalArgumentExceptionWhenRefundAmountIsNull() {
            // given
            String productId = "product123";
            Integer quantity = 1;

            // when & then
            assertThatThrownBy(() -> RefundDetail.of(productId, quantity, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refundAmount는 0 이상이어야 합니다.");
        }

        @Test
        @DisplayName("refundAmount가 음수이면 IllegalArgumentException 발생")
        void throwsIllegalArgumentExceptionWhenRefundAmountIsNegative() {
            // given
            String productId = "product123";
            Integer quantity = 1;
            Long refundAmount = -1L;

            // when & then
            assertThatThrownBy(() -> RefundDetail.of(productId, quantity, refundAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("refundAmount는 0 이상이어야 합니다.");
        }

        @Test
        @DisplayName("refundAmount가 0이면 객체 생성 성공")
        void createRefundDetailSuccessWhenRefundAmountIsZero() {
            // given
            String productId = "product123";
            Integer quantity = 1;
            Long refundAmount = 0L;

            // when
            RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);

            // then
            assertThat(refundDetail).isNotNull();
            assertThat(refundDetail.getRefundAmount()).isZero();
        }

        @Test
        @DisplayName("refundAmount가 큰 양수이면 객체 생성 성공")
        void createRefundDetailSuccessWhenRefundAmountIsLarge() {
            // given
            String productId = "product123";
            Integer quantity = 1;
            Long refundAmount = 999999999L;

            // when
            RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);

            // then
            assertThat(refundDetail).isNotNull();
            assertThat(refundDetail.getRefundAmount()).isEqualTo(999999999L);
        }
    }

    @Nested
    @DisplayName("여러 필드 조합 테스트")
    class MultiplFieldsCombinationTest {

        @Test
        @DisplayName("모든 필드가 최소값일 때 객체 생성 성공")
        void createRefundDetailSuccessWithMinimumValues() {
            // given
            String productId = "p";
            Integer quantity = 1;
            Long refundAmount = 0L;

            // when
            RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);

            // then
            assertThat(refundDetail).isNotNull();
            assertThat(refundDetail.getProductId()).isEqualTo("p");
            assertThat(refundDetail.getQuantity()).isEqualTo(1);
            assertThat(refundDetail.getRefundAmount()).isZero();
        }

        @Test
        @DisplayName("quantity와 refundAmount가 일치하는 경우 객체 생성 성공")
        void createRefundDetailSuccessWhenQuantityMatchesAmount() {
            // given
            String productId = "product123";
            Integer quantity = 5;
            Long refundAmount = 5000L; // quantity * 1000

            // when
            RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);

            // then
            assertThat(refundDetail).isNotNull();
            assertThat(refundDetail.getQuantity()).isEqualTo(5);
            assertThat(refundDetail.getRefundAmount()).isEqualTo(5000L);
        }
    }
}
