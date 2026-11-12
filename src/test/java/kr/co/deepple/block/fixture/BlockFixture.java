package kr.co.deepple.block.fixture;

import kr.co.deepple.block.domain.Block;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockFixture {

    private Long id;
    private long blockerId = 1L;
    private long blockedId = 2L;

    public static BlockFixture builder() {
        return new BlockFixture();
    }

    public Block build() {
        Block block = Block.of(blockerId, blockedId);
        setField(block, "id", id);
        return block;
    }
}
