package kr.co.deepple.heart.application.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record AdminHeartTransactionSearchCondition(
    String nickname,
    String phoneNumber,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdDateGoe,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdDateLoe) {}
