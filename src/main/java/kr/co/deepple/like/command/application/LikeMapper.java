package kr.co.deepple.like.command.application;

import static lombok.AccessLevel.PRIVATE;

import kr.co.deepple.like.command.domain.LikeLevel;
import kr.co.deepple.like.presentation.LikeLevelRequest;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class LikeMapper {

  public static LikeLevel toLikeLevel(LikeLevelRequest likeLevelRequest) {
    return switch (likeLevelRequest) {
      case INTERESTED -> LikeLevel.INTERESTED;
      case HIGHLY_INTERESTED -> LikeLevel.HIGHLY_INTERESTED;
    };
  }
}
