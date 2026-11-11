package kr.co.deepple.block.application.provided;

public interface BlockCommander {
    void createBlock(Long blockerId, Long blockedId);

    void createBlockIfAbsent(long blockerId, long blockedId);
}
