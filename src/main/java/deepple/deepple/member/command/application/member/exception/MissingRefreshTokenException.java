package deepple.deepple.member.command.application.member.exception;

public class MissingRefreshTokenException extends RuntimeException {
    public MissingRefreshTokenException() {
        super("리프레시 토큰이 누락되었습니다.");
    }
}