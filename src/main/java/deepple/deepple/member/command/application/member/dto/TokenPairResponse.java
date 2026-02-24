package deepple.deepple.member.command.application.member.dto;

public record TokenPairResponse(
    String accessToken,
    String refreshToken
) {
}