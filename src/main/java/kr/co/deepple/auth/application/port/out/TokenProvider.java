package kr.co.deepple.auth.application.port.out;

import java.time.Instant;
import kr.co.deepple.common.enums.Role;

public interface TokenProvider {
  String createAccessToken(long id, Role role, Instant issuedAt);
}
