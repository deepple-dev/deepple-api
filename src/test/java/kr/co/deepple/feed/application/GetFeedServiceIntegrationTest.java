package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedBlockFixture;
import kr.co.deepple.feed.fixture.OfficialFeedFixture;
import kr.co.deepple.feed.fixture.PollFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("피드 상세 조회 통합 테스트")
class GetFeedServiceIntegrationTest extends IntegrationTestSupport {

  private static final long VIEWER_MEMBER_ID = 1L;
  private static final long WRITER_MEMBER_ID = 2L;

  @Autowired private GetFeedService getFeedService;

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
  @DisplayName("같은 등급의 공개 피드를 작성자 정보와 함께 조회한다")
  void getPublishedFeedOfSameGrade() {
    // given
    Feed feed =
        feedRepository.save(
            PollFeedFixture.builder().authorId(writer.getId()).grade("GOLD").build());

    // when
    FeedView view = getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId());

    // then
    assertThat(view.type()).isEqualTo("POLL");
    assertThat(view.author().memberId()).isEqualTo(WRITER_MEMBER_ID);
    assertThat(view.background().backgroundId()).isEqualTo(1L);
    assertThat(view.isMine()).isFalse();
    assertThat(view.likeCount()).isZero();
    assertThat(view.poll().options()).extracting("content").containsExactly("예", "아니오");
    assertThat(view.poll().isResultVisible()).isFalse();
    assertThat(view.poll().participantCount()).isNull();
  }

  @Test
  @DisplayName("다른 등급, 심사 대기, 신고로 숨겨진 남의 피드는 없는 피드로 다룬다")
  void hideInvisibleFeedsOfOthers() {
    // given
    Feed otherGrade =
        feedRepository.save(
            TextFeedFixture.builder().authorId(writer.getId()).grade("SILVER").build());
    Feed pending =
        feedRepository.save(
            TextFeedFixture.builder()
                .authorId(writer.getId())
                .grade("GOLD")
                .image(image())
                .build());
    Feed hidden =
        feedRepository.save(
            TextFeedFixture.builder()
                .authorId(writer.getId())
                .grade("GOLD")
                .status(FeedStatus.HIDDEN_BY_REPORT)
                .build());

    // when & then
    for (Feed feed : new Feed[] {otherGrade, pending, hidden}) {
      assertThatThrownBy(() -> getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId()))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    }
  }

  @Test
  @DisplayName("내 피드는 등급과 상태에 상관없이 조회하고, 신고로 숨겨졌는지 알려준다")
  void getMyFeedRegardlessOfGradeAndStatus() {
    // given
    Feed feed =
        feedRepository.save(
            TextFeedFixture.builder()
                .authorId(viewer.getId())
                .grade("SILVER")
                .status(FeedStatus.HIDDEN_BY_REPORT)
                .build());

    // when
    FeedView view = getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId());

    // then
    assertThat(view.isMine()).isTrue();
    assertThat(view.isHiddenByReport()).isTrue();
  }

  @Test
  @DisplayName("비활성화 회원이나 차단 관계인 회원의 피드는 없는 피드로 다룬다")
  void hideFeedsOfInactiveOrBlockedAuthor() {
    // given
    FeedAuthor dormant =
        feedAuthorRepository.save(
            FeedAuthorFixture.builder()
                .memberId(3L)
                .activityStatus("DORMANT")
                .grade("GOLD")
                .build());
    Feed dormantFeed =
        feedRepository.save(
            TextFeedFixture.builder().authorId(dormant.getId()).grade("GOLD").build());
    Feed blockedFeed =
        feedRepository.save(
            TextFeedFixture.builder().authorId(writer.getId()).grade("GOLD").build());
    feedBlockRepository.save(
        FeedBlockFixture.builder().blockerId(WRITER_MEMBER_ID).blockedId(VIEWER_MEMBER_ID).build());

    // when & then
    for (Feed feed : new Feed[] {dormantFeed, blockedFeed}) {
      assertThatThrownBy(() -> getFeedService.getFeed(VIEWER_MEMBER_ID, feed.getId()))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    }
  }

  @Test
  @DisplayName("관리자 피드는 보여줄 등급이 비어 있거나 내 등급을 포함할 때만 보인다")
  void getOfficialFeedByTargetGrades() {
    // given
    FeedAuthor official =
        feedAuthorRepository.save(FeedAuthorFixture.builder().memberId(99L).build());
    setOfficial(official);
    Feed forAll =
        feedRepository.save(
            OfficialFeedFixture.builder().authorId(official.getId()).isPinned(true).build());
    Feed forDiamond =
        feedRepository.save(
            OfficialFeedFixture.builder()
                .authorId(official.getId())
                .targetGrades(Set.of("DIAMOND"))
                .build());

    // when
    FeedView view = getFeedService.getFeed(VIEWER_MEMBER_ID, forAll.getId());

    // then
    assertThat(view.type()).isEqualTo("OFFICIAL");
    assertThat(view.isPinned()).isTrue();
    assertThat(view.images()).hasSize(1);
    assertThat(view.textPosition()).isNull();
    assertThatThrownBy(() -> getFeedService.getFeed(VIEWER_MEMBER_ID, forDiamond.getId()))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
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
