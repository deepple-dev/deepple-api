package kr.co.deepple.auth.domain;

import java.time.Instant;
import kr.co.deepple.common.enums.Role;

public interface TokenProvider {
  String createAccessToken(long id, Role role, Instant issuedAt);
}
