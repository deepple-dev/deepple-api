package kr.co.deepple.community.application.dto;

import java.time.LocalDate;

public record AdminSelfIntroductionSearchCondition(
    Boolean isOpened,
    String nickname,
    String phoneNumber,
    LocalDate startDate,
    LocalDate endDate) {}
