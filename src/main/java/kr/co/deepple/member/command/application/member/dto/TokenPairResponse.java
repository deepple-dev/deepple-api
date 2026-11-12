package kr.co.deepple.member.command.application.member.dto;

public record TokenPairResponse(
    String accessToken,
    String refreshToken
) {
}