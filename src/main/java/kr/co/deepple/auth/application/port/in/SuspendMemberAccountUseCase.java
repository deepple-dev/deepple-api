package kr.co.deepple.auth.application.port.in;

import java.time.Instant;

public interface SuspendMemberAccountUseCase {
  void suspendTemporarily(long memberId, Instant suspendedUntil);

  void suspendPermanently(long memberId);
}
