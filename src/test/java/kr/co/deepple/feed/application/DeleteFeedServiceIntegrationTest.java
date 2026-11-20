package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("피드 삭제 통합 테스트")
class DeleteFeedServiceIntegrationTest extends IntegrationTestSupport {

  private static final long MEMBER_ID = 1L;
  private static final long OTHER_MEMBER_ID = 2L;

  @Autowired private DeleteFeedService deleteFeedService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  private FeedAuthor author;

  @BeforeEach
  void setUp() {
    author = feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(MEMBER_ID).build());
  }

  @Test
  @DisplayName("작성자가 피드를 지우면 행은 남기고 삭제 시각을 기록한다")
  void deleteOwnFeed() {
    // given
    long feedId =
        feedRepository.save(TextFeedFixture.builder().authorId(author.getId()).build()).getId();

    // when
    deleteFeedService.deleteFeed(MEMBER_ID, feedId);

    // then
    assertThat(feedRepository.findById(feedId).orElseThrow().isDeleted()).isTrue();
    assertThat(feedRepository.findByIdAndDeletedAtIsNull(feedId)).isEmpty();
  }

  @Test
  @DisplayName("다른 사람의 피드나 이미 지운 피드는 없는 피드로 거절한다")
  void rejectDeletingOthersFeed() {
    // given
    FeedAuthor other =
        feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(OTHER_MEMBER_ID).build());
    long feedId =
        feedRepository.save(TextFeedFixture.builder().authorId(other.getId()).build()).getId();

    // when & then
    assertThatThrownBy(() -> deleteFeedService.deleteFeed(MEMBER_ID, feedId))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    deleteFeedService.deleteFeed(OTHER_MEMBER_ID, feedId);
    assertThatThrownBy(() -> deleteFeedService.deleteFeed(OTHER_MEMBER_ID, feedId))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
  }
}
