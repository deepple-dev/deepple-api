package deepple.deepple.auth.domain;

import deepple.deepple.common.enums.Role;

public interface TokenParser {

    boolean isValid(String token);

    boolean isExpired(String token);

    long getId(String token);

    Role getRole(String token);
}
