package kr.co.deepple.member.application.port.in;

import java.time.LocalDate;

public record MemberPageSearchCondition(
    String activityStatus,
    String gender,
    String grade,
    String nicknameKeyword,
    String phoneNumber,
    LocalDate joinedFrom,
    LocalDate joinedTo) {}
