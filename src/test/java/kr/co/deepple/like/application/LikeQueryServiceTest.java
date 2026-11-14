package kr.co.deepple.like.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import kr.co.deepple.like.adapter.out.persistence.LikeQueryRepository;
import kr.co.deepple.like.application.dto.LikeViews;
import kr.co.deepple.like.application.dto.RawLikeView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeQueryServiceTest {

  private static final int CLIENT_PAGE_SIZE = 12;

  @InjectMocks private LikeQueryService likeQueryService;

  @Mock private LikeQueryRepository likeQueryRepository;

  private List<RawLikeView> createRawLikes(int count) {
    return IntStream.range(0, count)
        .mapToObj(i ->
            new RawLikeView(
                i + 1L,
                i + 100L,
                "https://example.com/image" + i,
                "nickname" + i,
                "SEOUL",
                1990 + i,
                i % 2 == 0,
                LocalDateTime.now()))
        .toList();
  }

  @Nested
  @DisplayName("보낸 좋아요 목록 조회")
  class FindSentLikes {

    @Test
    @DisplayName("데이터가 없는 경우 빈 목록을 반환한다")
    void returnsEmptyListWhenNoData() {
      // given
      long senderId = 1L;
      Long lastLikeId = null;
      given(likeQueryRepository.findSentLikes(senderId, lastLikeId)).willReturn(List.of());

      // when
      LikeViews result = likeQueryService.findSentLikes(senderId, lastLikeId);

      // then
      assertThat(result.likes()).isEmpty();
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE보다 적은 데이터가 있는 경우 모든 데이터를 반환한다")
    void returnsAllDataWhenLessThanPageSize() {
      // given
      long senderId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE - 1);
      given(likeQueryRepository.findSentLikes(senderId, lastLikeId)).willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findSentLikes(senderId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE - 1);
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE와 같은 수의 데이터가 있는 경우 hasMore는 false이다")
    void returnsExactPageSizeData() {
      // given
      long senderId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE);
      given(likeQueryRepository.findSentLikes(senderId, lastLikeId)).willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findSentLikes(senderId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE);
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE보다 많은 데이터가 있는 경우 페이지 크기만큼만 반환한고, hasMore는 true이다")
    void returnsLimitedDataWhenMoreThanPageSize() {
      // given
      long senderId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE + 1);
      given(likeQueryRepository.findSentLikes(senderId, lastLikeId)).willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findSentLikes(senderId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE);
      assertThat(result.hasMore()).isTrue();
    }
  }

  @Nested
  @DisplayName("받은 좋아요 목록 조회")
  class FindReceivedLikes {

    @Test
    @DisplayName("데이터가 없는 경우 빈 목록을 반환한다")
    void returnsEmptyListWhenNoData() {
      // given
      long receiverId = 1L;
      Long lastLikeId = null;
      given(likeQueryRepository.findReceivedLikes(receiverId, lastLikeId)).willReturn(List.of());

      // when
      LikeViews result = likeQueryService.findReceivedLikes(receiverId, lastLikeId);

      // then
      assertThat(result.likes()).isEmpty();
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE보다 적은 데이터가 있는 경우 모든 데이터를 반환한다")
    void returnsAllDataWhenLessThanPageSize() {
      // given
      long receiverId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE - 1);
      given(likeQueryRepository.findReceivedLikes(receiverId, lastLikeId)).willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findReceivedLikes(receiverId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE - 1);
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE와 같은 수의 데이터가 있는 경우 hasMore는 false이다")
    void returnsExactPageSizeData() {
      // given
      long receiverId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE);
      given(likeQueryRepository.findReceivedLikes(receiverId, lastLikeId)).willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findReceivedLikes(receiverId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE);
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE보다 많은 데이터가 있는 경우 페이지 크기만큼만 반환하고, hasMore는 true이다")
    void returnsLimitedDataWhenMoreThanPageSize() {
      // given
      long receiverId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE + 1);
      given(likeQueryRepository.findReceivedLikes(receiverId, lastLikeId)).willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findReceivedLikes(receiverId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE);
      assertThat(result.hasMore()).isTrue();
    }
  }
}
