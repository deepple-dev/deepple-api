package deepple.deepple.auth.application;

public enum AuthErrorStatus {
    MISSING_ACCESS_TOKEN,
    MISSING_REFRESH_TOKEN,
    INVALID_ACCESS_TOKEN,
    INVALID_REFRESH_TOKEN,
    EXPIRED_ACCESS_TOKEN,
    EXPIRED_REFRESH_TOKEN
}
