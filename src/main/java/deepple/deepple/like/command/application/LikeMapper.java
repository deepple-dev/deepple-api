package deepple.deepple.like.command.application;

import deepple.deepple.like.command.domain.LikeLevel;
import deepple.deepple.like.presentation.LikeLevelRequest;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class LikeMapper {

    public static LikeLevel toLikeLevel(LikeLevelRequest likeLevelRequest) {
        return switch (likeLevelRequest) {
            case INTERESTED -> LikeLevel.INTERESTED;
            case HIGHLY_INTERESTED -> LikeLevel.HIGHLY_INTERESTED;
        };
    }
}
