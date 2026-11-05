package deepple.deepple.auth.domain.exception;

public class MissingRefreshTokenException extends RuntimeException {
    public MissingRefreshTokenException() {
        super("리프레시 토큰이 없습니다.");
    }
}
