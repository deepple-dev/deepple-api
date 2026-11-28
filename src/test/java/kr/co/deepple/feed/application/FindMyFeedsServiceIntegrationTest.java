package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;
import java.util.stream.IntStream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedGridItemView;
import kr.co.deepple.feed.application.dto.FeedGridViews;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.dto.FeedViews;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.PollFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("내가 쓴 글 조회 통합 테스트")
class FindMyFeedsServiceIntegrationTest extends IntegrationTestSupport {

  private static final long VIEWER_MEMBER_ID = 1L;
  private static final long WRITER_MEMBER_ID = 2L;

  @Autowired private FindMyFeedsService findMyFeedsService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedBlockRepository feedBlockRepository;

  private FeedAuthor viewer;
  private FeedAuthor writer;

  @BeforeEach
  void setUp() {
    viewer = feedAuthorRepository.save(activeMember(VIEWER_MEMBER_ID, "GOLD"));
    writer = feedAuthorRepository.save(activeMember(WRITER_MEMBER_ID, "GOLD"));
  }

  @Test
  @DisplayName("내가 쓴 글을 최신순으로 20개씩 나눠 주고, 지운 피드와 남의 피드는 뺀다")
  void getMyFeedsByCursor() {
    // given
    IntStream.rangeClosed(1, 22)
        .forEach(index ->
            feedRepository.save(
                TextFeedFixture.builder().authorId(viewer.getId()).content("글 " + index).build()));
    Feed deleted = TextFeedFixture.builder().authorId(viewer.getId()).build();
    deleted.delete();
    feedRepository.save(deleted);
    feedRepository.save(TextFeedFixture.builder().authorId(writer.getId()).build());

    // when
    FeedGridViews first = findMyFeedsService.findMyFeeds(VIEWER_MEMBER_ID, null);
    FeedGridViews second = findMyFeedsService.findMyFeeds(VIEWER_MEMBER_ID, first.nextCursor());

    // then
    assertThat(first.feeds()).hasSize(20);
    assertThat(first.feeds().getFirst().content()).isEqualTo("글 22");
    assertThat(first.hasMore()).isTrue();
    assertThat(first.totalCount()).isEqualTo(22L);
    assertThat(second.feeds()).extracting(FeedGridItemView::content).containsExactly("글 2", "글 1");
    assertThat(second.hasMore()).isFalse();
    assertThat(second.nextCursor()).isNull();
    assertThat(second.totalCount()).isEqualTo(22L);
  }

  @Test
  @DisplayName("격자 칸 썸네일은 사진이 있으면 사진, 없으면 배경의 썸네일이다")
  void gridThumbnail() {
    // given
    feedRepository.save(
        TextFeedFixture.builder().authorId(viewer.getId()).backgroundId(2L).build());
    feedRepository.save(TextFeedFixture.builder().authorId(viewer.getId()).image(image()).build());

    // when
    FeedGridViews views = findMyFeedsService.findMyFeeds(VIEWER_MEMBER_ID, null);

    // then
    assertThat(views.feeds().get(0).thumbnailUrl()).endsWith("/images/members/1/1-thumbnail.webp");
    assertThat(views.feeds().get(1).thumbnailUrl())
        .endsWith("/feed-backgrounds/2/v1/thumbnail.webp");
  }

  @Test
  @DisplayName("투표 피드는 질문과 함께 선택지 내용을 주고, 투표 피드가 아니면 선택지는 빈 목록이다")
  void pollOptions() {
    // given
    feedRepository.save(TextFeedFixture.builder().authorId(viewer.getId()).build());
    feedRepository.save(
        PollFeedFixture.builder()
            .authorId(viewer.getId())
            .question("주말에 뭐 할까요?")
            .options(List.of("영화", "산책", "집콕"))
            .build());

    // when
    FeedGridViews views = findMyFeedsService.findMyFeeds(VIEWER_MEMBER_ID, null);

    // then
    FeedGridItemView poll = views.feeds().get(0);
    assertThat(poll.content()).isEqualTo("주말에 뭐 할까요?");
    assertThat(poll.pollOptions()).containsExactly("영화", "산책", "집콕");
    assertThat(views.feeds().get(1).pollOptions()).isEmpty();
  }

  @Test
  @DisplayName("하나씩 보기 목록은 격자와 같은 순서·커서로 피드 상세 모양을 주고, 내 투표 피드는 결과가 보인다")
  void getMyFeedViewsWithSameCursorAsGrid() {
    // given
    IntStream.rangeClosed(1, 21)
        .forEach(index ->
            feedRepository.save(
                TextFeedFixture.builder().authorId(viewer.getId()).content("글 " + index).build()));
    feedRepository.save(
        PollFeedFixture.builder()
            .authorId(viewer.getId())
            .question("주말에 뭐 할까요?")
            .options(List.of("영화", "산책"))
            .build());

    // when
    FeedGridViews grid = findMyFeedsService.findMyFeeds(VIEWER_MEMBER_ID, null);
    FeedViews first = findMyFeedsService.findMyFeedViews(VIEWER_MEMBER_ID, null);
    FeedViews second = findMyFeedsService.findMyFeedViews(VIEWER_MEMBER_ID, first.nextCursor());

    // then
    assertThat(first.feeds())
        .extracting(FeedView::feedId)
        .containsExactlyElementsOf(grid.feeds().stream().map(FeedGridItemView::feedId).toList());
    assertThat(first.nextCursor()).isEqualTo(grid.nextCursor());
    FeedView poll = first.feeds().getFirst();
    assertThat(poll.isMine()).isTrue();
    assertThat(poll.poll().isResultVisible()).isTrue();
    assertThat(poll.createdAt()).isNotNull();
    assertThat(second.feeds()).extracting(FeedView::content).containsExactly("글 2", "글 1");
    assertThat(second.hasMore()).isFalse();
  }

  @Test
  @DisplayName("숫자가 아닌 커서는 거절한다")
  void rejectInvalidCursor() {
    assertThatThrownBy(() -> findMyFeedsService.findMyFeeds(VIEWER_MEMBER_ID, "abc"))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_CURSOR);
  }

  private static FeedAuthor activeMember(long memberId, String grade) {
    return FeedAuthorFixture.builder()
        .memberId(memberId)
        .activityStatus("ACTIVE")
        .grade(grade)
        .build();
  }

  private static FeedImage image() {
    return FeedImage.of(1L, "images/members/1/1.webp", "images/members/1/1-thumbnail.webp", 0);
  }

  private void setOfficial(FeedAuthor author) {
    setField(author, "authorType", FeedAuthorType.OFFICIAL);
    feedAuthorRepository.save(author);
  }
}
