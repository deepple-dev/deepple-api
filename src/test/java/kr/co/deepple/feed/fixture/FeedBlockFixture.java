package kr.co.deepple.feed.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.feed.domain.FeedBlock;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedBlockFixture {

  private Long id;
  private long blockerId = 1L;
  private long blockedId = 2L;

  public static FeedBlockFixture builder() {
    return new FeedBlockFixture();
  }

  public FeedBlock build() {
    FeedBlock block = FeedBlock.of(blockerId, blockedId);
    setField(block, "id", id);
    return block;
  }
}
