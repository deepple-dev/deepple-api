package kr.co.deepple.payment.query.heartpurchaseoption.condition;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record HeartPurchaseOptionSearchCondition(
    String productId,
    String name,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdDateGoe,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdDateLoe) {}
