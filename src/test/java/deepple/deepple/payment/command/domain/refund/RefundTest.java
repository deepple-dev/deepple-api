package deepple.deepple.payment.command.domain.refund;

import deepple.deepple.payment.command.domain.order.Order;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundTest {

    @Nested
    @DisplayName("Refund 객체 생성 테스트")
    class CreateRefundTest {

        @Test
        @DisplayName("유효한 파라미터로 Refund 객체 생성 성공")
        void createRefundSuccessWhenParametersAreValid() {
            // given
            Order order = Order.of(1L, "transaction123", PaymentMethod.APP_STORE);
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);
            NotificationType notificationType = NotificationType.REFUND;

            // when
            Refund refund = Refund.of(order, refundDetail, notificationType);

            // then
            assertThat(refund).isNotNull();
            assertThat(refund.getMemberId()).isEqualTo(1L);
            assertThat(refund.getTransactionId()).isEqualTo("transaction123");
            assertThat(refund.getPaymentMethod()).isEqualTo(PaymentMethod.APP_STORE);
            assertThat(refund.getNotificationType()).isEqualTo(NotificationType.REFUND);
            assertThat(refund.getRefundedAt()).isNotNull();
        }

        @Test
        @DisplayName("Order가 null이면 NullPointerException 발생")
        void throwsNullPointerExceptionWhenOrderIsNull() {
            // given
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);
            NotificationType notificationType = NotificationType.REFUND;

            // when & then
            assertThatThrownBy(() -> Refund.of(null, refundDetail, notificationType))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("RefundDetail이 null이면 NullPointerException 발생")
        void throwsNullPointerExceptionWhenRefundDetailIsNull() {
            // given
            Order order = Order.of(1L, "transaction123", PaymentMethod.APP_STORE);
            NotificationType notificationType = NotificationType.REFUND;

            // when & then
            assertThatThrownBy(() -> Refund.of(order, null, notificationType))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("NotificationType이 null이면 NullPointerException 발생")
        void throwsNullPointerExceptionWhenNotificationTypeIsNull() {
            // given
            Order order = Order.of(1L, "transaction123", PaymentMethod.APP_STORE);
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);

            // when & then
            assertThatThrownBy(() -> Refund.of(order, refundDetail, null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Refund 객체의 getter 메서드 테스트")
    class RefundGetterTest {

        @Test
        @DisplayName("getProductId는 RefundDetail의 productId를 반환")
        void getProductIdReturnsProductIdFromRefundDetail() {
            // given
            Order order = Order.of(1L, "transaction123", PaymentMethod.APP_STORE);
            RefundDetail refundDetail = RefundDetail.of("product123", 2, 2000L);
            Refund refund = Refund.of(order, refundDetail, NotificationType.REFUND);

            // when
            String productId = refund.getProductId();

            // then
            assertThat(productId).isEqualTo("product123");
        }

        @Test
        @DisplayName("getQuantity는 RefundDetail의 quantity를 반환")
        void getQuantityReturnsQuantityFromRefundDetail() {
            // given
            Order order = Order.of(1L, "transaction123", PaymentMethod.APP_STORE);
            RefundDetail refundDetail = RefundDetail.of("product123", 3, 3000L);
            Refund refund = Refund.of(order, refundDetail, NotificationType.REFUND);

            // when
            Integer quantity = refund.getQuantity();

            // then
            assertThat(quantity).isEqualTo(3);
        }

        @Test
        @DisplayName("getRefundAmount는 RefundDetail의 refundAmount를 반환")
        void getRefundAmountReturnsRefundAmountFromRefundDetail() {
            // given
            Order order = Order.of(1L, "transaction123", PaymentMethod.APP_STORE);
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 5000L);
            Refund refund = Refund.of(order, refundDetail, NotificationType.REFUND);

            // when
            Long refundAmount = refund.getRefundAmount();

            // then
            assertThat(refundAmount).isEqualTo(5000L);
        }

        @Test
        @DisplayName("Order의 정보가 Refund 객체에 복사되어 저장됨")
        void orderInformationIsCopiedToRefund() {
            // given
            Long memberId = 123L;
            String transactionId = "txn456";
            PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
            Order order = Order.of(memberId, transactionId, paymentMethod);
            RefundDetail refundDetail = RefundDetail.of("product789", 1, 1000L);

            // when
            Refund refund = Refund.of(order, refundDetail, NotificationType.REFUND);

            // then
            assertThat(refund.getMemberId()).isEqualTo(memberId);
            assertThat(refund.getTransactionId()).isEqualTo(transactionId);
            assertThat(refund.getPaymentMethod()).isEqualTo(paymentMethod);
        }
    }

    @Nested
    @DisplayName("다양한 NotificationType 처리 테스트")
    class NotificationTypeHandlingTest {

        @Test
        @DisplayName("REFUND NotificationType으로 Refund 객체 생성")
        void createRefundWithRefundNotificationType() {
            // given
            Order order = Order.of(1L, "transaction123", PaymentMethod.APP_STORE);
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);

            // when
            Refund refund = Refund.of(order, refundDetail, NotificationType.REFUND);

            // then
            assertThat(refund.getNotificationType()).isEqualTo(NotificationType.REFUND);
        }

        @Test
        @DisplayName("REFUND_DECLINED NotificationType으로 Refund 객체 생성")
        void createRefundWithRefundDeclinedNotificationType() {
            // given
            Order order = Order.of(1L, "transaction123", PaymentMethod.APP_STORE);
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);

            // when
            Refund refund = Refund.of(order, refundDetail, NotificationType.REFUND_DECLINED);

            // then
            assertThat(refund.getNotificationType()).isEqualTo(NotificationType.REFUND_DECLINED);
        }

        @Test
        @DisplayName("UNKNOWN NotificationType으로 Refund 객체 생성")
        void createRefundWithUnknownNotificationType() {
            // given
            Order order = Order.of(1L, "transaction123", PaymentMethod.APP_STORE);
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);

            // when
            Refund refund = Refund.of(order, refundDetail, NotificationType.UNKNOWN);

            // then
            assertThat(refund.getNotificationType()).isEqualTo(NotificationType.UNKNOWN);
        }
    }
}