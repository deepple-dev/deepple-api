package kr.co.deepple.block.application.port.in;

public interface CheckBlockUseCase {
  boolean isBlocked(long blockerId, long blockedId);
}
