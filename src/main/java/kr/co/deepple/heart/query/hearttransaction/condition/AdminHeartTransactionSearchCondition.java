package kr.co.deepple.heart.query.hearttransaction.condition;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AdminHeartTransactionSearchCondition(
    String nickname,
    String phoneNumber,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate createdDateGoe,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate createdDateLoe
) {
}
