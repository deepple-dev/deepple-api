package kr.co.deepple.block.application.port.in;

import java.util.Set;

public interface FindBlockedMemberIdsUseCase {
  Set<Long> findBlockedMemberIds(long blockerId);
}
