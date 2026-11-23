package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedCreateRequest;
import kr.co.deepple.feed.application.dto.FeedTextPosition;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.dto.FeedViews;
import kr.co.deepple.feed.application.port.in.SyncFeedAuthorUseCase;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedBlockFixture;
import kr.co.deepple.feed.fixture.OfficialFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("홈 피드 조회 통합 테스트")
class FindHomeFeedsServiceIntegrationTest extends IntegrationTestSupport {

  private static final long VIEWER_MEMBER_ID = 1L;
  private static final long WRITER_MEMBER_ID = 2L;
  private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 11, 1, 12, 0);

  @Autowired private FindHomeFeedsService findHomeFeedsService;

  @Autowired private CreateFeedService createFeedService;

  @Autowired private DeleteFeedService deleteFeedService;

  @Autowired private SyncFeedAuthorUseCase syncFeedAuthorUseCase;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedBlockRepository feedBlockRepository;

  @Autowired private LikeFeedService likeFeedService;

  private FeedAuthor viewer;
  private FeedAuthor writer;
  private FeedAuthor official;

  @BeforeEach
  void setUp() {
    viewer = feedAuthorRepository.save(activeMember(VIEWER_MEMBER_ID, "GOLD"));
    writer = feedAuthorRepository.save(activeMember(WRITER_MEMBER_ID, "GOLD"));
    official = feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(99L).build());
    setField(official, "authorType", FeedAuthorType.OFFICIAL);
    feedAuthorRepository.save(official);
  }

  @Test
  @DisplayName("같은 등급의 사용자 피드와 내 등급이 대상인 관리자 피드를 고정 여부, 공개 시각 최신순으로 보여준다")
  void showFeedsOfMyGradeInOrder() {
    // given
    Feed oldText = saveUserFeed(writer, "GOLD", 1);
    Feed newText = saveUserFeed(writer, "GOLD", 3);
    Feed officialForAll = saveOfficialFeed(Set.of(), false, 2);
    Feed pinnedOld = saveOfficialFeed(Set.of("GOLD"), true, 0);
    saveUserFeed(writer, "SILVER", 4);
    saveOfficialFeed(Set.of("DIAMOND"), false, 5);

    // when
    FeedViews views = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);

    // then
    assertThat(views.feeds())
        .extracting(FeedView::feedId)
        .containsExactly(
            pinnedOld.getId(), newText.getId(), officialForAll.getId(), oldText.getId());
    assertThat(views.feeds().getFirst().isPinned()).isTrue();
    assertThat(views.feeds().get(1).author().memberId()).isEqualTo(WRITER_MEMBER_ID);
    assertThat(views.feeds().get(1).isMine()).isFalse();
    assertThat(views.hasMore()).isFalse();
    assertThat(views.nextCursor()).isNull();
  }

  @Test
  @DisplayName("내 피드, 공개되지 않은 피드, 차단 관계(양방향)와 비활성화 회원의 피드는 뺀다")
  void excludeInvisibleFeeds() {
    // given
    Feed visible = saveUserFeed(writer, "GOLD", 0);
    saveUserFeed(viewer, "GOLD", 1);
    feedRepository.save(
        TextFeedFixture.builder()
            .authorId(writer.getId())
            .grade("GOLD")
            .status(FeedStatus.HIDDEN_BY_REPORT)
            .publishedAt(BASE_TIME)
            .build());
    feedRepository.save(
        TextFeedFixture.builder().authorId(writer.getId()).grade("GOLD").image(image()).build());
    FeedAuthor blocker = feedAuthorRepository.save(activeMember(3L, "GOLD"));
    FeedAuthor blocked = feedAuthorRepository.save(activeMember(4L, "GOLD"));
    FeedAuthor dormant =
        feedAuthorRepository.save(
            FeedAuthorFixture.builder()
                .memberId(5L)
                .activityStatus("DORMANT")
                .grade("GOLD")
                .build());
    saveUserFeed(blocker, "GOLD", 2);
    saveUserFeed(blocked, "GOLD", 3);
    saveUserFeed(dormant, "GOLD", 4);
    feedBlockRepository.save(
        FeedBlockFixture.builder().blockerId(3L).blockedId(VIEWER_MEMBER_ID).build());
    feedBlockRepository.save(
        FeedBlockFixture.builder().blockerId(VIEWER_MEMBER_ID).blockedId(4L).build());

    // when
    FeedViews views = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);

    // then
    assertThat(views.feeds()).extracting(FeedView::feedId).containsExactly(visible.getId());
  }

  @Test
  @DisplayName("고정 피드가 21개면 1페이지는 고정 20개, 2페이지는 고정 1개와 일반 피드다")
  void pinnedFeedsContinueToNextPage() {
    // given
    List<Long> pinnedIds = new ArrayList<>();
    for (int minutes = 21; minutes >= 1; minutes--) {
      pinnedIds.add(saveOfficialFeed(Set.of(), true, minutes).getId());
    }
    List<Long> unpinnedIds = new ArrayList<>();
    for (int minutes = 30; minutes >= 1; minutes--) {
      unpinnedIds.add(saveUserFeed(writer, "GOLD", minutes).getId());
    }

    // when
    FeedViews first = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);
    FeedViews second = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, first.nextCursor());
    FeedViews third = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, second.nextCursor());

    // then
    assertThat(first.feeds()).extracting(FeedView::feedId).isEqualTo(pinnedIds.subList(0, 20));
    assertThat(first.hasMore()).isTrue();
    assertThat(second.feeds())
        .extracting(FeedView::feedId)
        .containsExactlyElementsOf(concat(pinnedIds.subList(20, 21), unpinnedIds.subList(0, 19)));
    assertThat(third.feeds())
        .extracting(FeedView::feedId)
        .containsExactlyElementsOf(unpinnedIds.subList(19, 30));
    assertThat(third.hasMore()).isFalse();
  }

  @Test
  @DisplayName("걸러낸 피드만큼 적게 주고, 다음 커서로 이어서 읽는다")
  void returnFewerFeedsAfterFiltering() {
    // given
    FeedAuthor blocked = saveBlockedAuthor();
    IntStream.rangeClosed(1, 5).forEach(minutes -> saveUserFeed(blocked, "GOLD", 100 + minutes));
    List<Long> visibleIds = saveVisibleFeeds(25);

    // when
    FeedViews first = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);
    FeedViews second = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, first.nextCursor());

    // then
    assertThat(first.feeds()).extracting(FeedView::feedId).isEqualTo(visibleIds.subList(0, 15));
    assertThat(first.hasMore()).isTrue();
    assertThat(second.feeds()).extracting(FeedView::feedId).isEqualTo(visibleIds.subList(15, 25));
    assertThat(second.hasMore()).isFalse();
  }

  @Test
  @DisplayName("한 페이지가 모두 걸러져도 더 있으면 빈 목록과 다음 커서를 준다")
  void returnEmptyPageWithNextCursor() {
    // given
    FeedAuthor blocked = saveBlockedAuthor();
    IntStream.rangeClosed(1, 25).forEach(minutes -> saveUserFeed(blocked, "GOLD", 100 + minutes));
    List<Long> visibleIds = saveVisibleFeeds(5);

    // when
    FeedViews first = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);
    FeedViews second = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, first.nextCursor());

    // then
    assertThat(first.feeds()).isEmpty();
    assertThat(first.hasMore()).isTrue();
    assertThat(second.feeds()).extracting(FeedView::feedId).isEqualTo(visibleIds);
    assertThat(second.hasMore()).isFalse();
  }

  @Test
  @DisplayName("목록을 채운 뒤 공개된 피드는 목록에 들어가고, 지운 피드는 빠진다")
  void addPublishedFeedAndRemoveDeletedFeed() {
    // given
    Feed existing = saveUserFeed(writer, "GOLD", 0);
    findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);

    // when
    long createdId =
        createFeedService
            .createFeed(WRITER_MEMBER_ID, UUID.randomUUID().toString(), textRequest())
            .feedId();
    awaitEventProcessing();
    FeedViews afterCreate = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);
    deleteFeedService.deleteFeed(WRITER_MEMBER_ID, existing.getId());
    awaitEventProcessing();
    FeedViews afterDelete = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);

    // then
    assertThat(afterCreate.feeds())
        .extracting(FeedView::feedId)
        .containsExactly(createdId, existing.getId());
    assertThat(afterDelete.feeds()).extracting(FeedView::feedId).containsExactly(createdId);
  }

  @Test
  @DisplayName("목록을 채우기 전에 공개된 피드가 먼저 들어가도, 조회할 때 DB에서 전체를 다시 채운다")
  void reloadWhenFeedAddedBeforeLoading() {
    // given
    Feed existing = saveUserFeed(writer, "GOLD", 0);

    // when
    long createdId =
        createFeedService
            .createFeed(WRITER_MEMBER_ID, UUID.randomUUID().toString(), textRequest())
            .feedId();
    awaitEventProcessing();
    FeedViews views = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);

    // then
    assertThat(views.feeds())
        .extracting(FeedView::feedId)
        .containsExactly(createdId, existing.getId());
  }

  @Test
  @DisplayName("작성자가 비활성화되면 피드가 빠지고, 다시 활성화되면 돌아온다")
  void removeAndRestoreFeedsByActivity() {
    // given
    Feed feed = saveUserFeed(writer, "GOLD", 0);
    findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);

    // when
    syncFeedAuthorUseCase.syncActivityStatus(
        WRITER_MEMBER_ID, "DORMANT", Instant.parse("2026-11-02T00:00:00Z"));
    awaitEventProcessing();
    FeedViews afterDormant = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);
    syncFeedAuthorUseCase.syncActivityStatus(
        WRITER_MEMBER_ID, "ACTIVE", Instant.parse("2026-11-03T00:00:00Z"));
    awaitEventProcessing();
    FeedViews afterActive = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);

    // then
    assertThat(afterDormant.feeds()).isEmpty();
    assertThat(afterActive.feeds()).extracting(FeedView::feedId).containsExactly(feed.getId());
  }

  @Test
  @DisplayName("좋아요 수와 내가 좋아요를 눌렀는지 함께 보여준다")
  void showLikeCountAndIsLiked() {
    // given
    Feed liked = saveUserFeed(writer, "GOLD", 1);
    Feed notLiked = saveUserFeed(writer, "GOLD", 0);
    findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);
    likeFeedService.likeFeed(VIEWER_MEMBER_ID, liked.getId());

    // when
    FeedViews views = findHomeFeedsService.findHomeFeeds(VIEWER_MEMBER_ID, null);

    // then
    assertThat(views.feeds())
        .extracting(FeedView::feedId, FeedView::likeCount, FeedView::isLiked)
        .containsExactly(tuple(liked.getId(), 1L, true), tuple(notLiked.getId(), 0L, false));
  }

  @Test
  @DisplayName("등급이 없는 회원에게는 빈 목록을 준다")
  void emptyForMemberWithoutGrade() {
    // given
    feedAuthorRepository.save(
        FeedAuthorFixture.builder().memberId(6L).activityStatus("ACTIVE").build());
    saveUserFeed(writer, "GOLD", 0);

    // when
    FeedViews views = findHomeFeedsService.findHomeFeeds(6L, null);

    // then
    assertThat(views.feeds()).isEmpty();
    assertThat(views.hasMore()).isFalse();
  }

  private Feed saveUserFeed(FeedAuthor author, String grade, int minutes) {
    return feedRepository.save(
        TextFeedFixture.builder()
            .authorId(author.getId())
            .grade(grade)
            .publishedAt(BASE_TIME.plusMinutes(minutes))
            .build());
  }

  private Feed saveOfficialFeed(Set<String> targetGrades, boolean isPinned, int minutes) {
    return feedRepository.save(
        OfficialFeedFixture.builder()
            .authorId(official.getId())
            .targetGrades(targetGrades)
            .isPinned(isPinned)
            .publishedAt(BASE_TIME.plusMinutes(minutes))
            .build());
  }

  private static FeedAuthor activeMember(long memberId, String grade) {
    return FeedAuthorFixture.builder()
        .memberId(memberId)
        .activityStatus("ACTIVE")
        .grade(grade)
        .build();
  }

  private static FeedImage image() {
    return FeedImage.of(1L, "images/members/2/1.webp", "images/members/2/1-thumbnail.webp", 0);
  }

  private static FeedCreateRequest textRequest() {
    return new FeedCreateRequest(
        "TEXT", "새로 쓴 피드", new FeedTextPosition(0.5, 0.5, 0.0, 1.0), 1L, null, null);
  }

  private static List<Long> concat(List<Long> first, List<Long> second) {
    List<Long> result = new ArrayList<>(first);
    result.addAll(second);
    return result;
  }

  private FeedAuthor saveBlockedAuthor() {
    FeedAuthor blocked = feedAuthorRepository.save(activeMember(4L, "GOLD"));
    feedBlockRepository.save(
        FeedBlockFixture.builder().blockerId(VIEWER_MEMBER_ID).blockedId(4L).build());
    return blocked;
  }

  private List<Long> saveVisibleFeeds(int count) {
    return IntStream.iterate(count, minutes -> minutes >= 1, minutes -> minutes - 1)
        .mapToObj(minutes -> saveUserFeed(writer, "GOLD", minutes).getId())
        .toList();
  }
}
