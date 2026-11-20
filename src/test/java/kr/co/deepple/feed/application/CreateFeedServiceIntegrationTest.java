package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedCreateRequest;
import kr.co.deepple.feed.application.dto.FeedTextPosition;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("피드 등록 Idempotency-Key 통합 테스트")
class CreateFeedServiceIntegrationTest extends IntegrationTestSupport {

  private static final long MEMBER_ID = 1L;
  private static final String KEY = "8e03978e-40d5-43e8-bc93-6894a57f9324";
  private static final FeedTextPosition CENTER = new FeedTextPosition(0.5, 0.5, 0.0, 1.0);

  @Autowired private CreateFeedService createFeedService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @BeforeEach
  void setUp() {
    feedAuthorRepository.save(
        FeedAuthorFixture.builder().memberId(MEMBER_ID).grade("GOLD").build());
  }

  @Test
  @DisplayName("같은 키로 다시 보내면 새로 만들지 않고 처음 만든 피드를 돌려준다")
  void replayCompletedRequest() {
    // given
    FeedCreateRequest request = textRequest("오늘 날씨가 좋아요", 1L);
    FeedView first = createFeedService.createFeed(MEMBER_ID, KEY, request);

    // when
    FeedView retry = createFeedService.createFeed(MEMBER_ID, KEY, request);

    // then
    assertThat(retry.feedId()).isEqualTo(first.feedId());
    assertThat(feedRepository.count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("같은 키로 동시에 보내도 피드는 하나만 만들고 둘 다 그 피드를 받는다")
  void createOnceConcurrently() {
    // given
    FeedCreateRequest request = textRequest("오늘 날씨가 좋아요", 1L);

    // when
    List<Long> feedIds =
        IntStream.range(0, 5)
            .mapToObj(i ->
                CompletableFuture.supplyAsync(
                    () -> createFeedService.createFeed(MEMBER_ID, KEY, request).feedId()))
            .toList()
            .stream()
            .map(CompletableFuture::join)
            .toList();

    // then
    assertThat(feedIds).containsOnly(feedIds.getFirst());
    assertThat(feedRepository.count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("등록에 실패한 키는 같은 키로 다시 보낼 수 있다")
  void retryAfterFailure() {
    // given
    FeedCreateRequest request = textRequest("오늘 날씨가 좋아요", 1L);
    assertErrorCode(
        () -> createFeedService.createFeed(MEMBER_ID, KEY, textRequest("없는 배경", 999_999L)),
        FeedErrorCode.FEED_BACKGROUND_NOT_FOUND);

    // when
    FeedView view = createFeedService.createFeed(MEMBER_ID, KEY, request);

    // then
    assertThat(feedRepository.findById(view.feedId())).isPresent();
  }

  @Test
  @DisplayName("키는 회원마다 따로 쓴다")
  void scopeKeyByMember() {
    // given
    feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(2L).grade("GOLD").build());
    FeedCreateRequest request = textRequest("오늘 날씨가 좋아요", 1L);

    // when
    FeedView mine = createFeedService.createFeed(MEMBER_ID, KEY, request);
    FeedView others = createFeedService.createFeed(2L, KEY, request);

    // then
    assertThat(others.feedId()).isNotEqualTo(mine.feedId());
  }

  private static FeedCreateRequest textRequest(String content, long backgroundId) {
    return new FeedCreateRequest("TEXT", content, CENTER, backgroundId, null, null);
  }

  private static void assertErrorCode(Runnable action, FeedErrorCode errorCode) {
    assertThatThrownBy(action::run)
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(errorCode);
  }
}
