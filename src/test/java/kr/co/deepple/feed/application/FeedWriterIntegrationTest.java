package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedCreateRequest;
import kr.co.deepple.feed.application.dto.FeedTextPosition;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedPollOption;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.PollFeed;
import kr.co.deepple.feed.domain.TextFeed;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.image.application.port.out.ImageRepository;
import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.fixture.ImageFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("피드 저장 통합 테스트")
class FeedWriterIntegrationTest extends IntegrationTestSupport {

  private static final long MEMBER_ID = 1L;
  private static final long OTHER_MEMBER_ID = 2L;
  private static final FeedTextPosition CENTER = new FeedTextPosition(0.5, 0.5, 0.0, 1.0);

  @Autowired private FeedWriter feedWriter;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private ImageRepository imageRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private TransactionTemplate transactionTemplate;

  private FeedAuthor author;

  @BeforeEach
  void setUp() {
    author =
        feedAuthorRepository.save(
            FeedAuthorFixture.builder().memberId(MEMBER_ID).grade("GOLD").build());
  }

  @Test
  @DisplayName("배경 글 피드는 작성자의 지금 등급으로 바로 공개된다")
  void createBackgroundTextFeed() {
    // given
    FeedCreateRequest request = new FeedCreateRequest("TEXT", "첫 줄\n둘째 줄", CENTER, 3L, null, null);

    // when
    long feedId = feedWriter.create(author, UUID.randomUUID().toString(), request);

    // then
    TextFeed feed = (TextFeed) feedRepository.findById(feedId).orElseThrow();
    assertThat(feed.getAuthorId()).isEqualTo(author.getId());
    assertThat(feed.getGrade()).isEqualTo("GOLD");
    assertThat(feed.getStatus()).isEqualTo(FeedStatus.PUBLISHED);
    assertThat(feed.getPublishedAt()).isNotNull();
    assertThat(feed.getBackgroundId()).isEqualTo(3L);
    assertThat(feed.getTextPosition().x()).isEqualTo(0.5);
  }

  @Test
  @DisplayName("사진 피드는 처리 중인 이미지로도 등록되고 심사 대기 상태가 된다")
  void createPhotoFeed() {
    // given
    Image image = imageRepository.save(ImageFixture.builder().memberId(MEMBER_ID).build());
    FeedCreateRequest request =
        new FeedCreateRequest("TEXT", "사진 피드", CENTER, null, image.getId(), null);

    // when
    long feedId = feedWriter.create(author, UUID.randomUUID().toString(), request);

    // then
    List<FeedImage> images =
        transactionTemplate.execute(status -> {
          TextFeed feed = (TextFeed) feedRepository.findById(feedId).orElseThrow();
          assertThat(feed.getStatus()).isEqualTo(FeedStatus.PENDING);
          assertThat(feed.getPublishedAt()).isNull();
          return List.copyOf(feed.getImages());
        });
    assertThat(images)
        .singleElement()
        .satisfies(feedImage -> {
          assertThat(feedImage.getImageId()).isEqualTo(image.getId());
          assertThat(feedImage.getImageKey()).isEqualTo(image.getImageKey());
          assertThat(feedImage.getThumbnailKey()).isEqualTo(image.getThumbnailKey());
        });
  }

  @Test
  @DisplayName("투표 피드는 선택지를 보낸 순서대로 저장한다")
  void createPollFeed() {
    // given
    FeedCreateRequest request =
        new FeedCreateRequest("POLL", "주말에 뭐 할까요?", CENTER, 1L, null, List.of("영화", "산책", "집"));

    // when
    long feedId = feedWriter.create(author, UUID.randomUUID().toString(), request);

    // then
    List<String> options =
        transactionTemplate.execute(status ->
            ((PollFeed) feedRepository.findById(feedId).orElseThrow())
                .getOptions().stream().map(FeedPollOption::getContent).toList());
    assertThat(options).containsExactly("영화", "산책", "집");
  }

  @Test
  @DisplayName("글 피드에 투표 선택지를 보내면 거절한다")
  void rejectPollOptionsOnTextFeed() {
    FeedCreateRequest request =
        new FeedCreateRequest("TEXT", "글", CENTER, 1L, null, List.of("예", "아니오"));

    assertThatThrownBy(() -> feedWriter.create(author, UUID.randomUUID().toString(), request))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_POLL_OPTIONS);
  }

  @Test
  @DisplayName("사용자는 관리자 피드를 등록할 수 없다")
  void rejectOfficialType() {
    FeedCreateRequest request = new FeedCreateRequest("OFFICIAL", "공지", CENTER, 1L, null, null);

    assertThatThrownBy(() -> feedWriter.create(author, UUID.randomUUID().toString(), request))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_TYPE);
  }

  @Test
  @DisplayName("숨긴 배경이나 없는 배경으로는 등록할 수 없다")
  void rejectHiddenBackground() {
    jdbcTemplate.update("update feed_backgrounds set is_hidden = true where id = 9");
    try {
      FeedCreateRequest request = new FeedCreateRequest("TEXT", "글", CENTER, 9L, null, null);

      assertThatThrownBy(() -> feedWriter.create(author, UUID.randomUUID().toString(), request))
          .isInstanceOf(DeeppleException.class)
          .extracting("errorCode")
          .isEqualTo(FeedErrorCode.FEED_BACKGROUND_NOT_FOUND);
    } finally {
      jdbcTemplate.update("update feed_backgrounds set is_hidden = false where id = 9");
    }
  }
}
