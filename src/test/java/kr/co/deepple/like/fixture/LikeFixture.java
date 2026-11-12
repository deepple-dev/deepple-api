package kr.co.deepple.like.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.like.command.domain.Like;
import kr.co.deepple.like.command.domain.LikeLevel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeFixture {

  private Long id;
  private long senderId = 1L;
  private long receiverId = 2L;
  private LikeLevel level = LikeLevel.INTERESTED;

  public static LikeFixture builder() {
    return new LikeFixture();
  }

  public Like build() {
    Like like = Like.of(senderId, receiverId, level);
    setField(like, "id", id);
    return like;
  }
}
