package kr.co.deepple.member.application.port.in;

public record MemberPreview(
    long memberId, String nickname, String profileImageUrl, String city, Integer yearOfBirth) {}
