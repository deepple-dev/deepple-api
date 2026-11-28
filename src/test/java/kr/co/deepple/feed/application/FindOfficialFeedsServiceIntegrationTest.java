package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.LocalDateTime;
import java.util.Set;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.dto.FeedViews;
import kr.co.deepple.feed.application.dto.OfficialFeedGridItemView;
import kr.co.deepple.feed.application.dto.OfficialFeedGridViews;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedCountFixture;
import kr.co.deepple.feed.fixture.OfficialFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("관리자 피드 모아보기 통합 테스트")
class FindOfficialFeedsServiceIntegrationTest extends IntegrationTestSupport {

  private static final long VIEWER_MEMBER_ID = 1L;
  private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 11, 1, 12, 0);

  @Autowired private FindOfficialFeedsService findOfficialFeedsService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedCountRepository feedCountRepository;

  private FeedAuthor official;

  @BeforeEach
  void setUp() {
    feedAuthorRepository.save(
        FeedAuthorFixture.builder()
            .memberId(VIEWER_MEMBER_ID)
            .activityStatus("ACTIVE")
            .grade("GOLD")
            .build());
    official = feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(99L).build());
    setField(official, "authorType", FeedAuthorType.OFFICIAL);
    feedAuthorRepository.save(official);
  }

  @Test
  @DisplayName("내 등급에 보이는 공개 관리자 피드만 고정 피드부터 최신순으로 보여준다")
  void showVisibleOfficialFeedsInOrder() {
    // given
    Feed old = saveOfficialFeed(Set.of(), false, true, 1);
    Feed recent = saveOfficialFeed(Set.of("GOLD"), false, true, 3);
    Feed multiGrade = saveOfficialFeed(Set.of("GOLD", "DIAMOND"), false, true, 2);
    Feed pinned = saveOfficialFeed(Set.of(), true, true, 0);
    saveOfficialFeed(Set.of(), false, false, 4);
    saveOfficialFeed(Set.of("DIAMOND"), false, true, 5);
    feedRepository.save(TextFeedFixture.builder().authorId(1L).grade("GOLD").build());

    // when
    OfficialFeedGridViews views =
        findOfficialFeedsService.findOfficialFeeds(VIEWER_MEMBER_ID, null);

    // then
    assertThat(views.feeds())
        .extracting(OfficialFeedGridItemView::feedId)
        .containsExactly(pinned.getId(), recent.getId(), multiGrade.getId(), old.getId());
    assertThat(views.feeds().getFirst().thumbnailUrl())
        .endsWith("images/admins/1/1-thumbnail.webp");
    assertThat(views.hasMore()).isFalse();
    assertThat(views.totalCount()).isEqualTo(4L);
  }

  @Test
  @DisplayName("격자 칸에 관리자 피드의 내용과 좋아요 수를 담는다")
  void gridItemContentAndLikeCount() {
    // given
    Feed feed =
        feedRepository.save(
            OfficialFeedFixture.builder()
                .authorId(official.getId())
                .content("12월 이벤트 안내")
                .publishedAt(BASE_TIME)
                .build());
    feedCountRepository.save(FeedCountFixture.builder().feedId(feed.getId()).likeCount(3L).build());

    // when
    OfficialFeedGridViews views =
        findOfficialFeedsService.findOfficialFeeds(VIEWER_MEMBER_ID, null);

    // then
    OfficialFeedGridItemView item = views.feeds().getFirst();
    assertThat(item.content()).isEqualTo("12월 이벤트 안내");
    assertThat(item.likeCount()).isEqualTo(3L);
  }

  @Test
  @DisplayName("하나씩 보기 목록은 격자와 같은 순서·커서로 관리자 작성자와 사진을 담은 피드 상세 모양을 준다")
  void showOfficialFeedViewsWithSameCursorAsGrid() {
    // given
    for (int minutes = 1; minutes <= 12; minutes++) {
      saveOfficialFeed(Set.of(), true, true, minutes);
      saveOfficialFeed(Set.of(), false, true, minutes);
    }

    // when
    OfficialFeedGridViews grid = findOfficialFeedsService.findOfficialFeeds(VIEWER_MEMBER_ID, null);
    FeedViews first = findOfficialFeedsService.findOfficialFeedViews(VIEWER_MEMBER_ID, null);
    FeedViews second =
        findOfficialFeedsService.findOfficialFeedViews(VIEWER_MEMBER_ID, first.nextCursor());

    // then
    assertThat(first.feeds())
        .extracting(FeedView::feedId)
        .containsExactlyElementsOf(
            grid.feeds().stream().map(OfficialFeedGridItemView::feedId).toList());
    assertThat(first.nextCursor()).isEqualTo(grid.nextCursor());
    FeedView view = first.feeds().getFirst();
    assertThat(view.author().authorType()).isEqualTo("OFFICIAL");
    assertThat(view.images()).isNotEmpty();
    assertThat(view.isMine()).isFalse();
    assertThat(second.feeds()).hasSize(4);
    assertThat(second.hasMore()).isFalse();
  }

  @Test
  @DisplayName("하나씩 보기 목록도 내 등급에 보이지 않거나 공개되지 않은 관리자 피드는 뺀다")
  void excludeInvisibleOfficialFeedViews() {
    // given
    Feed visible = saveOfficialFeed(Set.of(), false, true, 1);
    saveOfficialFeed(Set.of(), false, false, 2);
    saveOfficialFeed(Set.of("DIAMOND"), false, true, 3);

    // when
    FeedViews views = findOfficialFeedsService.findOfficialFeedViews(VIEWER_MEMBER_ID, null);

    // then
    assertThat(views.feeds()).extracting(FeedView::feedId).containsExactly(visible.getId());
  }

  @Test
  @DisplayName("커서로 다음 페이지를 이어서 보여주고, 고정 피드에서 일반 피드로 넘어간다")
  void continueWithCursor() {
    // given
    for (int minutes = 1; minutes <= 15; minutes++) {
      saveOfficialFeed(Set.of(), true, true, minutes);
      saveOfficialFeed(Set.of(), false, true, minutes);
    }

    // when
    OfficialFeedGridViews first =
        findOfficialFeedsService.findOfficialFeeds(VIEWER_MEMBER_ID, null);
    OfficialFeedGridViews second =
        findOfficialFeedsService.findOfficialFeeds(VIEWER_MEMBER_ID, first.nextCursor());

    // then
    assertThat(first.feeds()).hasSize(20);
    assertThat(first.hasMore()).isTrue();
    assertThat(second.feeds()).hasSize(10);
    assertThat(second.hasMore()).isFalse();
    assertThat(second.nextCursor()).isNull();
    assertThat(first.totalCount()).isEqualTo(30L);
    assertThat(second.totalCount()).isEqualTo(30L);
    assertThat(first.feeds())
        .extracting(OfficialFeedGridItemView::feedId)
        .doesNotContainAnyElementsOf(
            second.feeds().stream().map(OfficialFeedGridItemView::feedId).toList());
  }

  private Feed saveOfficialFeed(
      Set<String> targetGrades, boolean isPinned, boolean isPublished, int minutes) {
    return feedRepository.save(
        OfficialFeedFixture.builder()
            .authorId(official.getId())
            .targetGrades(targetGrades)
            .isPinned(isPinned)
            .isPublished(isPublished)
            .publishedAt(isPublished ? BASE_TIME.plusMinutes(minutes) : null)
            .build());
  }
}
