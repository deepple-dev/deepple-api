package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedLikeFixture;
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

  @Autowired private FeedLikeRepository feedLikeRepository;

  @Autowired private FeedCountReader feedCountReader;

  @Autowired private FeedLikeCountCacheRepository feedLikeCountCacheRepository;

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

  @Test
  @DisplayName("피드를 지우면 좋아요 수 카운터를 지우고 좋아요 기록은 남긴다")
  void deleteLikeCounterAndKeepLikes() {
    // given
    long feedId =
        feedRepository.save(TextFeedFixture.builder().authorId(author.getId()).build()).getId();
    feedLikeRepository.save(FeedLikeFixture.builder().feedId(feedId).memberId(2L).build());
    feedCountReader.read(feedId);

    // when
    deleteFeedService.deleteFeed(MEMBER_ID, feedId);
    awaitEventProcessing();

    // then
    assertThat(feedLikeCountCacheRepository.findAll(List.of(feedId))).isEmpty();
    assertThat(feedLikeRepository.count()).isEqualTo(1L);
  }
}
