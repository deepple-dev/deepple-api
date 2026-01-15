package deepple.deepple.payment.command.domain.refund;

import deepple.deepple.payment.command.domain.order.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
            Long orderId = 1L;
            Long memberId = 1L;
            String transactionId = "transaction123";
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);
            PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
            NotificationType notificationType = NotificationType.REFUND;
            LocalDateTime refundedAt = LocalDateTime.now();

            // when
            Refund refund = Refund.of(orderId, memberId, transactionId, refundDetail, paymentMethod, notificationType,
                refundedAt);

            // then
            assertThat(refund).isNotNull();
            assertThat(refund.getOrderId()).isEqualTo(orderId);
            assertThat(refund.getMemberId()).isEqualTo(memberId);
            assertThat(refund.getTransactionId()).isEqualTo(transactionId);
            assertThat(refund.getPaymentMethod()).isEqualTo(PaymentMethod.APP_STORE);
            assertThat(refund.getNotificationType()).isEqualTo(NotificationType.REFUND);
            assertThat(refund.getRefundedAt()).isEqualTo(refundedAt);
        }

        @Test
        @DisplayName("orderId가 null이면 NullPointerException 발생")
        void throwsNullPointerExceptionWhenOrderIdIsNull() {
            // given
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);
            NotificationType notificationType = NotificationType.REFUND;
            LocalDateTime refundedAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(
                () -> Refund.of(null, 1L, "transaction123", refundDetail, PaymentMethod.APP_STORE, notificationType,
                    refundedAt))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("RefundDetail이 null이면 NullPointerException 발생")
        void throwsNullPointerExceptionWhenRefundDetailIsNull() {
            // given
            NotificationType notificationType = NotificationType.REFUND;
            LocalDateTime refundedAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(
                () -> Refund.of(1L, 1L, "transaction123", null, PaymentMethod.APP_STORE, notificationType, refundedAt))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("NotificationType이 null이면 NullPointerException 발생")
        void throwsNullPointerExceptionWhenNotificationTypeIsNull() {
            // given
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);
            LocalDateTime refundedAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(
                () -> Refund.of(1L, 1L, "transaction123", refundDetail, PaymentMethod.APP_STORE, null, refundedAt))
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
            RefundDetail refundDetail = RefundDetail.of("product123", 2, 2000L);
            LocalDateTime refundedAt = LocalDateTime.now();
            Refund refund = Refund.of(1L, 1L, "transaction123", refundDetail, PaymentMethod.APP_STORE,
                NotificationType.REFUND, refundedAt);

            // when
            String productId = refund.getProductId();

            // then
            assertThat(productId).isEqualTo("product123");
        }

        @Test
        @DisplayName("getQuantity는 RefundDetail의 quantity를 반환")
        void getQuantityReturnsQuantityFromRefundDetail() {
            // given
            RefundDetail refundDetail = RefundDetail.of("product123", 3, 3000L);
            LocalDateTime refundedAt = LocalDateTime.now();
            Refund refund = Refund.of(1L, 1L, "transaction123", refundDetail, PaymentMethod.APP_STORE,
                NotificationType.REFUND, refundedAt);

            // when
            Integer quantity = refund.getQuantity();

            // then
            assertThat(quantity).isEqualTo(3);
        }

        @Test
        @DisplayName("getRefundAmount는 RefundDetail의 refundAmount를 반환")
        void getRefundAmountReturnsRefundAmountFromRefundDetail() {
            // given
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 5000L);
            LocalDateTime refundedAt = LocalDateTime.now();
            Refund refund = Refund.of(1L, 1L, "transaction123", refundDetail, PaymentMethod.APP_STORE,
                NotificationType.REFUND, refundedAt);

            // when
            Long refundAmount = refund.getRefundAmount();

            // then
            assertThat(refundAmount).isEqualTo(5000L);
        }

        @Test
        @DisplayName("파라미터 정보가 Refund 객체에 저장됨")
        void parameterInformationIsSavedToRefund() {
            // given
            Long orderId = 100L;
            Long memberId = 123L;
            String transactionId = "txn456";
            PaymentMethod paymentMethod = PaymentMethod.APP_STORE;
            RefundDetail refundDetail = RefundDetail.of("product789", 1, 1000L);
            LocalDateTime refundedAt = LocalDateTime.now();

            // when
            Refund refund = Refund.of(orderId, memberId, transactionId, refundDetail, paymentMethod,
                NotificationType.REFUND, refundedAt);

            // then
            assertThat(refund.getOrderId()).isEqualTo(orderId);
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
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);
            LocalDateTime refundedAt = LocalDateTime.now();

            // when
            Refund refund = Refund.of(1L, 1L, "transaction123", refundDetail, PaymentMethod.APP_STORE,
                NotificationType.REFUND, refundedAt);

            // then
            assertThat(refund.getNotificationType()).isEqualTo(NotificationType.REFUND);
        }

        @Test
        @DisplayName("REFUND_DECLINED NotificationType으로 Refund 객체 생성")
        void createRefundWithRefundDeclinedNotificationType() {
            // given
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);
            LocalDateTime refundedAt = LocalDateTime.now();

            // when
            Refund refund = Refund.of(1L, 1L, "transaction123", refundDetail, PaymentMethod.APP_STORE,
                NotificationType.REFUND_DECLINED, refundedAt);

            // then
            assertThat(refund.getNotificationType()).isEqualTo(NotificationType.REFUND_DECLINED);
        }

        @Test
        @DisplayName("UNKNOWN NotificationType으로 Refund 객체 생성")
        void createRefundWithUnknownNotificationType() {
            // given
            RefundDetail refundDetail = RefundDetail.of("product123", 1, 1000L);
            LocalDateTime refundedAt = LocalDateTime.now();

            // when
            Refund refund = Refund.of(1L, 1L, "transaction123", refundDetail, PaymentMethod.APP_STORE,
                NotificationType.UNKNOWN, refundedAt);

            // then
            assertThat(refund.getNotificationType()).isEqualTo(NotificationType.UNKNOWN);
        }
    }
}
