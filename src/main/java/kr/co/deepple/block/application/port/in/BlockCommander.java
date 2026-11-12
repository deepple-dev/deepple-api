package kr.co.deepple.block.application.port.in;

public interface BlockCommander {
  void createBlock(long blockerId, long blockedId);

  void createBlockIfAbsent(long blockerId, long blockedId);
}
