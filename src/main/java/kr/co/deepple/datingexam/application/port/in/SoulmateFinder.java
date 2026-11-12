package kr.co.deepple.datingexam.application.port.in;

import java.util.Set;

public interface SoulmateFinder {
  Set<Long> findSoulmateIds(long memberId);
}
