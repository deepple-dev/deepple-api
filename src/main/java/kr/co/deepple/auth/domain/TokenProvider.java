package kr.co.deepple.auth.domain;

import kr.co.deepple.common.enums.Role;

import java.time.Instant;

public interface TokenProvider {

    String createAccessToken(long id, Role role, Instant issuedAt);
}
