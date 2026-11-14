package kr.co.deepple.like.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.co.deepple.like.application.port.out.LikeCommandRepository;
import kr.co.deepple.like.domain.LikeLevel;
import kr.co.deepple.like.fixture.LikeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindLikeLevelServiceTest {

  @Mock private LikeCommandRepository likeCommandRepository;

  @InjectMocks private FindLikeLevelService findLikeLevelService;

  @Test
  @DisplayName("보낸 좋아요가 있으면 좋아요 단계 이름을 돌려준다.")
  void findLikeLevel() {
    given(likeCommandRepository.findBySenderIdAndReceiverId(1L, 2L))
        .willReturn(
            Optional.of(
                LikeFixture.builder()
                    .senderId(1L)
                    .receiverId(2L)
                    .level(LikeLevel.HIGHLY_INTERESTED)
                    .build()));

    assertThat(findLikeLevelService.findLikeLevel(1L, 2L)).contains("HIGHLY_INTERESTED");
  }

  @Test
  @DisplayName("보낸 좋아요가 없으면 빈 값을 돌려준다.")
  void returnEmptyWhenNoLike() {
    given(likeCommandRepository.findBySenderIdAndReceiverId(1L, 2L)).willReturn(Optional.empty());

    assertThat(findLikeLevelService.findLikeLevel(1L, 2L)).isEmpty();
  }
}
