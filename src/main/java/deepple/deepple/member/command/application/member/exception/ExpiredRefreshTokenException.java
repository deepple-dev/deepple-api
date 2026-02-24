package deepple.deepple.member.command.application.member.exception;

public class ExpiredRefreshTokenException extends RuntimeException {
    public ExpiredRefreshTokenException() {
        super("리프레시 토큰이 만료되었습니다.");
    }
}