package kr.co.deepple.member.application.port.in;

import java.time.LocalDateTime;

public record MemberScreeningProfile(
    long memberId, String nickname, Integer yearOfBirth, String gender, LocalDateTime joinedAt) {}
