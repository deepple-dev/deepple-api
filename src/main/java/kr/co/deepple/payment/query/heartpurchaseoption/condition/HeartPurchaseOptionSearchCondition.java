package kr.co.deepple.payment.query.heartpurchaseoption.condition;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record HeartPurchaseOptionSearchCondition(
    String productId,

    String name,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate createdDateGoe,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate createdDateLoe
) {
}
