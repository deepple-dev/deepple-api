package kr.co.deepple.common.security;

import kr.co.deepple.common.enums.Role;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtSpec {

    public static final String ACCESS_TOKEN_TYPE = "at+jwt";
    public static final String AUDIENCE = "deepple-api";
    public static final String ROLES_CLAIM = "roles";
    public static final String CLIENT_ID_CLAIM = "client_id";
    public static final String AUTHORITY_PREFIX = "ROLE_";

    private static final String APP_CLIENT_ID = "deepple-app";
    private static final String ADMIN_CLIENT_ID = "deepple-admin";

    public static String clientIdOf(Role role) {
        return switch (role) {
            case MEMBER -> APP_CLIENT_ID;
            case ADMIN, SUPER_ADMIN -> ADMIN_CLIENT_ID;
        };
    }

    public static String authorityOf(Role role) {
        return AUTHORITY_PREFIX + role.name();
    }
}
