package kr.co.deepple.block.application.port.in;

public interface CreateBlockUseCase {

  void createBlock(long blockerId, long blockedId);
}
