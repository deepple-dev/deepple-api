package kr.co.deepple.like.application;

import kr.co.deepple.like.application.dto.LikeLevelRequest;
import kr.co.deepple.like.domain.LikeLevel;
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
