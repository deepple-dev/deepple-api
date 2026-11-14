package kr.co.deepple.like.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.co.deepple.like.application.port.out.LikeRepository;
import kr.co.deepple.like.domain.Like;
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

  @Mock private LikeRepository likeRepository;

  @InjectMocks private FindLikeLevelService findLikeLevelService;

  @Test
  @DisplayName("보낸 좋아요가 있으면 좋아요 단계 이름을 돌려준다.")
  void findLikeLevel() {
    given(likeRepository.findBySenderIdAndReceiverId(1L, 2L))
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
    given(likeRepository.findBySenderIdAndReceiverId(1L, 2L)).willReturn(Optional.empty());

    assertThat(findLikeLevelService.findLikeLevel(1L, 2L)).isEmpty();
  }

  @Test
  @DisplayName("받은 회원별 좋아요 단계 이름을 돌려주고, 단계가 없는 좋아요는 넣지 않는다.")
  void findLikeLevels() {
    Like withoutLevel = mock(Like.class);
    given(withoutLevel.getLevel()).willReturn(null);
    given(likeRepository.findAllBySenderIdAndReceiverIdIn(1L, List.of(2L, 3L, 4L)))
        .willReturn(
            List.of(
                LikeFixture.builder()
                    .senderId(1L)
                    .receiverId(2L)
                    .level(LikeLevel.HIGHLY_INTERESTED)
                    .build(),
                withoutLevel));

    assertThat(findLikeLevelService.findLikeLevels(1L, List.of(2L, 3L, 4L)))
        .containsOnly(Map.entry(2L, "HIGHLY_INTERESTED"));
  }

  @Test
  @DisplayName("받은 회원 목록이 비어 있으면 조회하지 않고 빈 결과를 돌려준다.")
  void returnEmptyWhenNoReceiverIds() {
    assertThat(findLikeLevelService.findLikeLevels(1L, List.of())).isEmpty();
    verifyNoInteractions(likeRepository);
  }
}
