package deepple.deepple.auth.domain;

import deepple.deepple.common.enums.Role;

import java.time.Instant;

public interface TokenProvider {

    String createAccessToken(long id, Role role, Instant issuedAt);
}
