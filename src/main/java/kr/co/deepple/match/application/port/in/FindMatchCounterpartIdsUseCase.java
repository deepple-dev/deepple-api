package kr.co.deepple.match.application.port.in;

import java.util.Set;

public interface FindMatchCounterpartIdsUseCase {
  Set<Long> findMatchCounterpartIds(long memberId);
}
