package deepple.deepple.payment.query.refund.condition;

import java.time.LocalDate;

public record RefundSearchCondition(
    Long memberId,
    LocalDate refundedDateGoe,
    LocalDate refundedDateLoe
) {
}