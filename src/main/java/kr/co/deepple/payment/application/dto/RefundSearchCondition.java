package kr.co.deepple.payment.application.dto;

import java.time.LocalDate;

public record RefundSearchCondition(
    Long memberId, LocalDate refundedDateGoe, LocalDate refundedDateLoe) {}
