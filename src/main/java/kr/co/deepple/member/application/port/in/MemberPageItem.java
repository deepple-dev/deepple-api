package kr.co.deepple.member.application.port.in;

import java.time.LocalDateTime;

public record MemberPageItem(
    long memberId, String nickname, String gender, String activityStatus, LocalDateTime joinedAt) {}
