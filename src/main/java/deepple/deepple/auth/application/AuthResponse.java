package deepple.deepple.auth.application;

import deepple.deepple.common.enums.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AuthResponse {

    private final AuthStatus status;
    private final Long memberId;
    private final Role role;
    private final AuthErrorStatus errorStatus;

    public static AuthResponse authenticated(long memberId, Role role) {
        return new AuthResponse(AuthStatus.AUTHENTICATED, memberId, role, null);
    }

    public static AuthResponse error(AuthErrorStatus errorStatus) {
        return new AuthResponse(AuthStatus.ERROR, null, null, errorStatus);
    }

    public boolean isAuthenticated() {
        return status == AuthStatus.AUTHENTICATED;
    }

    private enum AuthStatus {
        AUTHENTICATED, ERROR
    }
}
