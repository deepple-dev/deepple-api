package kr.co.deepple.like.command.application;

import kr.co.deepple.like.command.domain.LikeLevel;
import kr.co.deepple.like.presentation.LikeLevelRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeMapper {

  public static LikeLevel toLikeLevel(LikeLevelRequest likeLevelRequest) {
    return switch (likeLevelRequest) {
      case INTERESTED -> LikeLevel.INTERESTED;
      case HIGHLY_INTERESTED -> LikeLevel.HIGHLY_INTERESTED;
    };
  }
}
