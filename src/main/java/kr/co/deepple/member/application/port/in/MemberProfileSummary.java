package kr.co.deepple.member.application.port.in;

import java.util.Set;

public record MemberProfileSummary(
    long memberId,
    String nickname,
    String profileImageUrl,
    Integer age,
    String gender,
    String city,
    String district,
    String mbti,
    String religion,
    Set<String> hobbies) {}
