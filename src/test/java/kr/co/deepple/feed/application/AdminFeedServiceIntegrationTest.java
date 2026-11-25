package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.AdminFeedCreateRequest;
import kr.co.deepple.feed.application.dto.AdminFeedUpdateRequest;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;
import kr.co.deepple.feed.application.dto.AdminUserFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminUserFeedView;
import kr.co.deepple.feed.application.dto.FeedImageView;
import kr.co.deepple.feed.application.dto.HomeFeedSortKey;
import kr.co.deepple.feed.application.port.out.FeedAuthorRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.application.port.out.HomeFeedCacheRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedImage;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.fixture.FeedAuthorFixture;
import kr.co.deepple.feed.fixture.FeedCommentFixture;
import kr.co.deepple.feed.fixture.OfficialFeedFixture;
import kr.co.deepple.feed.fixture.TextFeedFixture;
import kr.co.deepple.image.application.port.out.ImageRepository;
import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.domain.ImageErrorCode;
import kr.co.deepple.image.domain.ImageStatus;
import kr.co.deepple.image.fixture.ImageFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("관리자 피드 API 통합 테스트")
class AdminFeedServiceIntegrationTest extends IntegrationTestSupport {

  private static final long ADMIN_ID = 1L;
  private static final long OTHER_ADMIN_ID = 2L;
  private static final long MEMBER_ID = 1L;
  private static final PageRequest FIRST_PAGE = PageRequest.of(0, 100);

  @Autowired private FindAdminFeedsService findAdminFeedsService;

  @Autowired private CreateOfficialFeedService createOfficialFeedService;

  @Autowired private UpdateOfficialFeedService updateOfficialFeedService;

  @Autowired private ChangeAdminFeedStatusService changeAdminFeedStatusService;

  @Autowired private DeleteAdminFeedService deleteAdminFeedService;

  @Autowired private FeedRepository feedRepository;

  @Autowired private FeedAuthorRepository feedAuthorRepository;

  @Autowired private FeedCommentRepository feedCommentRepository;

  @Autowired private ImageRepository imageRepository;

  @Autowired private HomeFeedCacheRepository homeFeedCacheRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  private FeedAuthor official;
  private FeedAuthor member;

  @BeforeEach
  void setUp() {
    official = feedAuthorRepository.save(FeedAuthorFixture.official());
    member =
        feedAuthorRepository.save(
            FeedAuthorFixture.builder().memberId(MEMBER_ID).grade("GOLD").nickname("진저").build());
  }

  @Test
  @DisplayName("사용자 피드 목록에는 사용자 피드만 상태와 상관없이 최신순으로 작성자 닉네임과 함께 나온다")
  void findUserFeeds() {
    // given
    long pendingId = savePhotoFeed(ImageStatus.PROCESSING);
    saveOfficialFeed(true);
    long textId =
        feedRepository.save(TextFeedFixture.builder().authorId(member.getId()).build()).getId();

    // when
    Page<AdminUserFeedView> page =
        findAdminFeedsService.findUserFeeds(new AdminUserFeedSearchCondition(null), FIRST_PAGE);

    // then
    assertThat(page.getContent())
        .extracting(AdminUserFeedView::feedId)
        .containsExactly(textId, pendingId);
    assertThat(page.getContent()).extracting(AdminUserFeedView::authorNickname).containsOnly("진저");
  }

  @Test
  @DisplayName("상태로 찾으면 그 상태의 피드만 나온다")
  void findFeedsByStatus() {
    // given
    long pendingId = savePhotoFeed(ImageStatus.PROCESSING);
    feedRepository.save(TextFeedFixture.builder().authorId(member.getId()).build());

    // when
    Page<AdminUserFeedView> page =
        findAdminFeedsService.findUserFeeds(
            new AdminUserFeedSearchCondition("PENDING"), FIRST_PAGE);

    // then
    assertThat(page.getTotalElements()).isEqualTo(1L);
    assertThat(page.getContent()).extracting(AdminUserFeedView::feedId).containsExactly(pendingId);
  }

  @Test
  @DisplayName("관리자 피드 목록은 공개·숨김 피드를 최신순으로 보여주고 지운 피드는 뺀다")
  void findOfficialFeeds() {
    // given
    long hiddenId = saveOfficialFeed(false);
    long publishedId = saveOfficialFeed(true);
    long deletedId = saveOfficialFeed(true);
    deleteAdminFeedService.deleteOfficialFeed(deletedId);
    feedRepository.save(TextFeedFixture.builder().authorId(member.getId()).build());

    // when
    Page<AdminOfficialFeedView> page =
        findAdminFeedsService.findOfficialFeeds(
            new AdminOfficialFeedSearchCondition(null), FIRST_PAGE);

    // then
    assertThat(page.getContent())
        .extracting(AdminOfficialFeedView::feedId)
        .containsExactly(publishedId, hiddenId);
  }

  @Test
  @DisplayName("없는 상태로 찾으면 거절한다")
  void rejectInvalidCondition() {
    assertThatThrownBy(() ->
            findAdminFeedsService.findUserFeeds(
                new AdminUserFeedSearchCondition("WAITING"), FIRST_PAGE))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_STATUS);
  }

  @Test
  @DisplayName("관리자 피드는 공식 계정 작성자로 저장하고 공개하면 대상 등급의 홈 목록에 넣는다")
  void createOfficialFeed() {
    // given
    Image first = saveAdminImage(ADMIN_ID);
    Image second = saveAdminImage(ADMIN_ID);
    AdminFeedCreateRequest request =
        new AdminFeedCreateRequest(
            "11월 이벤트",
            "매일 접속하면 하트 두 배",
            List.of(second.getId(), first.getId()),
            List.of("GOLD"),
            true,
            true);

    // when
    AdminOfficialFeedView view =
        createOfficialFeedService.createOfficialFeed(
            ADMIN_ID, UUID.randomUUID().toString(), request);
    awaitEventProcessing();

    // then
    assertThat(view.status()).isEqualTo("PUBLISHED");
    assertThat(view.images())
        .extracting(FeedImageView::imageId)
        .containsExactly(second.getId(), first.getId());
    assertThat(view.targetGrades()).containsExactly("GOLD");
    assertThat(view.isPinned()).isTrue();
    assertThat(view.publishedAt()).isNotNull();
    assertThat(feedRepository.findById(view.feedId()).orElseThrow().getAuthorId())
        .isEqualTo(official.getId());
    assertThat(homeFeedIds("GOLD")).containsExactly(view.feedId());
    assertThat(homeFeedIds("SILVER")).isEmpty();
  }

  @Test
  @DisplayName("변환이 끝나지 않은 이미지로는 공개 상태로 등록할 수 없고, 비공개로는 등록할 수 있다")
  void rejectPublishingOfficialFeedWithUnprocessedImage() {
    long imageId = saveAdminImage(ADMIN_ID, ImageStatus.PROCESSING).getId();
    AdminFeedCreateRequest published =
        new AdminFeedCreateRequest("공지", "내용", List.of(imageId), null, true, false);
    AdminFeedCreateRequest hidden =
        new AdminFeedCreateRequest("공지", "내용", List.of(imageId), null, false, false);

    assertThatThrownBy(() ->
            createOfficialFeedService.createOfficialFeed(
                ADMIN_ID, UUID.randomUUID().toString(), published))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_IMAGE_NOT_PROCESSED);
    long feedId =
        createOfficialFeedService
            .createOfficialFeed(ADMIN_ID, UUID.randomUUID().toString(), hidden)
            .feedId();
    assertThatThrownBy(() ->
            changeAdminFeedStatusService.changeOfficialFeedStatus(feedId, FeedStatus.PUBLISHED))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_IMAGE_NOT_PROCESSED);
  }

  @Test
  @DisplayName("같은 Idempotency-Key로 다시 등록하면 처음 만든 피드를 돌려준다")
  void createOfficialFeedIdempotently() {
    // given
    String idempotencyKey = UUID.randomUUID().toString();
    AdminFeedCreateRequest request = createRequest(saveAdminImage(ADMIN_ID).getId());

    // when
    AdminOfficialFeedView first =
        createOfficialFeedService.createOfficialFeed(ADMIN_ID, idempotencyKey, request);
    AdminOfficialFeedView second =
        createOfficialFeedService.createOfficialFeed(ADMIN_ID, idempotencyKey, request);

    // then
    assertThat(second.feedId()).isEqualTo(first.feedId());
    assertThat(feedRepository.count()).isEqualTo(1L);
  }

  @Test
  @DisplayName("다른 관리자가 올린 이미지로는 등록할 수 있고, 회원이 올린 이미지로는 등록할 수 없다")
  void useImagesOfAnyAdmin() {
    long otherAdminImageId = saveAdminImage(OTHER_ADMIN_ID).getId();
    long memberImageId =
        imageRepository.save(ImageFixture.builder().memberId(MEMBER_ID).build()).getId();

    assertThat(
            createOfficialFeedService
                .createOfficialFeed(
                    ADMIN_ID, UUID.randomUUID().toString(), createRequest(otherAdminImageId))
                .images())
        .extracting(FeedImageView::imageId)
        .containsExactly(otherAdminImageId);
    assertThatThrownBy(() ->
            createOfficialFeedService.createOfficialFeed(
                ADMIN_ID, UUID.randomUUID().toString(), createRequest(memberImageId)))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.IMAGE_NOT_FOUND);
  }

  @Test
  @DisplayName("같은 이미지를 두 번 넣으면 거절한다")
  void rejectDuplicateImages() {
    long imageId = saveAdminImage(ADMIN_ID).getId();
    AdminFeedCreateRequest request =
        new AdminFeedCreateRequest("공지", "내용", List.of(imageId, imageId), null, true, false);

    assertThatThrownBy(() ->
            createOfficialFeedService.createOfficialFeed(
                ADMIN_ID, UUID.randomUUID().toString(), request))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_IMAGES);
  }

  @Test
  @DisplayName("변환이 끝난 사진 피드를 승인하면 공개되고 홈 목록에 들어간다")
  void approvePhotoFeed() {
    // given
    long feedId = savePhotoFeed(ImageStatus.COMPLETED);

    // when
    AdminUserFeedView view =
        changeAdminFeedStatusService.changeUserFeedStatus(feedId, FeedStatus.PUBLISHED);
    awaitEventProcessing();

    // then
    assertThat(view.status()).isEqualTo("PUBLISHED");
    assertThat(view.publishedAt()).isNotNull();
    assertThat(homeFeedIds("GOLD")).containsExactly(feedId);
  }

  @Test
  @DisplayName("변환이 끝나지 않은 사진 피드는 승인할 수 없고, 반려는 할 수 있다")
  void rejectApprovingUnprocessedPhoto() {
    // given
    long feedId = savePhotoFeed(ImageStatus.PROCESSING);

    // when & then
    assertThatThrownBy(
            () -> changeAdminFeedStatusService.changeUserFeedStatus(feedId, FeedStatus.PUBLISHED))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_IMAGE_NOT_PROCESSED);
    assertThat(
            changeAdminFeedStatusService.changeUserFeedStatus(feedId, FeedStatus.REJECTED).status())
        .isEqualTo("REJECTED");
  }

  @Test
  @DisplayName("공개된 사진 피드를 반려로 바꾸면 홈 목록에서 뺀다")
  void rejectPublishedPhotoFeed() {
    // given
    long feedId = savePhotoFeed(ImageStatus.COMPLETED);
    changeAdminFeedStatusService.changeUserFeedStatus(feedId, FeedStatus.PUBLISHED);
    awaitEventProcessing();
    assertThat(homeFeedIds("GOLD")).containsExactly(feedId);

    // when
    changeAdminFeedStatusService.changeUserFeedStatus(feedId, FeedStatus.REJECTED);
    awaitEventProcessing();

    // then
    assertThat(homeFeedIds("GOLD")).isEmpty();
  }

  @Test
  @DisplayName("사용자 피드는 관리자 피드 수정으로 고칠 수 없다")
  void rejectEditingUserFeed() {
    long feedId = savePhotoFeed(ImageStatus.COMPLETED);
    AdminFeedUpdateRequest request = new AdminFeedUpdateRequest(null, "바꾼 내용", null, null, null);

    assertThatThrownBy(() -> updateOfficialFeedService.updateOfficialFeed(feedId, request))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
  }

  @Test
  @DisplayName("관리자 피드를 고치면 보낸 값만 바꾸고 이미지는 보낸 목록으로 대체한다")
  void updateOfficialFeed() {
    // given
    Image kept = saveAdminImage(ADMIN_ID);
    Image removed = saveAdminImage(ADMIN_ID);
    Image added = saveAdminImage(OTHER_ADMIN_ID);
    long feedId =
        feedRepository
            .save(
                OfficialFeedFixture.builder()
                    .authorId(official.getId())
                    .images(List.of(feedImage(kept, 0), feedImage(removed, 1)))
                    .build())
            .getId();
    AdminFeedUpdateRequest request =
        new AdminFeedUpdateRequest(null, "바꾼 내용", List.of(added.getId(), kept.getId()), null, null);

    // when
    AdminOfficialFeedView view = updateOfficialFeedService.updateOfficialFeed(feedId, request);

    // then
    assertThat(view.title()).isEqualTo("공지");
    assertThat(view.content()).isEqualTo("바꾼 내용");
    assertThat(view.images())
        .extracting(FeedImageView::imageId)
        .containsExactly(added.getId(), kept.getId());
    assertThat(countFeedImageRows(feedId)).isEqualTo(2L);
  }

  @Test
  @DisplayName("관리자 피드의 대상 등급과 공개 여부를 바꾸면 홈 목록도 맞춘다")
  void syncHomeWhenOfficialFeedChanges() {
    // given
    long feedId = saveOfficialFeed(true);
    homeFeedCacheRepository.add("GOLD", new HomeFeedSortKey(feedId, 1L));
    homeFeedCacheRepository.add("SILVER", new HomeFeedSortKey(feedId, 1L));

    // when
    updateOfficialFeedService.updateOfficialFeed(
        feedId, new AdminFeedUpdateRequest(null, null, null, List.of("GOLD"), null));
    awaitEventProcessing();

    // then
    assertThat(homeFeedIds("GOLD")).containsExactly(feedId);
    assertThat(homeFeedIds("SILVER")).isEmpty();

    // when
    changeAdminFeedStatusService.changeOfficialFeedStatus(feedId, FeedStatus.HIDDEN);
    awaitEventProcessing();

    // then
    assertThat(homeFeedIds("GOLD")).isEmpty();
  }

  @Test
  @DisplayName("관리자 피드를 심사 상태로 바꿀 수 없다")
  void rejectReviewStatusOnOfficialFeed() {
    long feedId = saveOfficialFeed(true);

    assertThatThrownBy(() ->
            changeAdminFeedStatusService.changeOfficialFeedStatus(feedId, FeedStatus.REJECTED))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_STATUS_CHANGE);
  }

  @Test
  @DisplayName("사용자 피드 API로 관리자 피드를, 관리자 피드 API로 사용자 피드를 바꾸거나 지울 수 없다")
  void rejectOtherFeedType() {
    long userFeedId = savePhotoFeed(ImageStatus.COMPLETED);
    long officialFeedId = saveOfficialFeed(true);

    assertFeedNotFound(() ->
        changeAdminFeedStatusService.changeUserFeedStatus(officialFeedId, FeedStatus.PUBLISHED));
    assertFeedNotFound(() ->
        changeAdminFeedStatusService.changeOfficialFeedStatus(userFeedId, FeedStatus.PUBLISHED));
    assertFeedNotFound(() -> deleteAdminFeedService.deleteUserFeed(officialFeedId));
    assertFeedNotFound(() -> deleteAdminFeedService.deleteOfficialFeed(userFeedId));
  }

  @Test
  @DisplayName("관리자가 사용자 피드나 관리자 피드를 지우면 그 피드의 댓글을 soft delete한다")
  void softDeleteCommentsOnAdminDelete() {
    // given
    long userFeedId =
        feedRepository
            .save(TextFeedFixture.builder().authorId(member.getId()).grade("GOLD").build())
            .getId();
    long officialFeedId = saveOfficialFeed(true);
    FeedComment userFeedComment =
        feedCommentRepository.save(
            FeedCommentFixture.builder().feedId(userFeedId).authorId(member.getId()).build());
    FeedComment officialFeedComment =
        feedCommentRepository.save(
            FeedCommentFixture.builder().feedId(officialFeedId).authorId(member.getId()).build());

    // when
    deleteAdminFeedService.deleteUserFeed(userFeedId);
    deleteAdminFeedService.deleteOfficialFeed(officialFeedId);
    awaitEventProcessing();

    // then
    assertThat(feedCommentRepository.findById(userFeedComment.getId()))
        .get()
        .extracting(FeedComment::isDeleted)
        .isEqualTo(true);
    assertThat(feedCommentRepository.findById(officialFeedComment.getId()))
        .get()
        .extracting(FeedComment::isDeleted)
        .isEqualTo(true);
  }

  @Test
  @DisplayName("관리자는 사용자 피드도 지울 수 있고, 지우면 홈 목록에서 뺀다")
  void deleteUserFeed() {
    // given
    long feedId =
        feedRepository
            .save(TextFeedFixture.builder().authorId(member.getId()).grade("GOLD").build())
            .getId();
    homeFeedCacheRepository.add("GOLD", new HomeFeedSortKey(feedId, 1L));

    // when
    deleteAdminFeedService.deleteUserFeed(feedId);
    awaitEventProcessing();

    // then
    assertThat(feedRepository.findById(feedId).orElseThrow().isDeleted()).isTrue();
    assertThat(homeFeedIds("GOLD")).isEmpty();
    assertThatThrownBy(() -> deleteAdminFeedService.deleteUserFeed(feedId))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
  }

  @Test
  @DisplayName("신고로 숨겨진 피드도 지울 수 있다")
  void deleteHiddenByReport() {
    long feedId =
        feedRepository
            .save(
                TextFeedFixture.builder()
                    .authorId(member.getId())
                    .status(FeedStatus.HIDDEN_BY_REPORT)
                    .build())
            .getId();

    deleteAdminFeedService.deleteUserFeed(feedId);

    assertThat(feedRepository.findById(feedId).orElseThrow().isDeleted()).isTrue();
  }

  private static void assertFeedNotFound(Runnable action) {
    assertThatThrownBy(action::run)
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
  }

  private long savePhotoFeed(ImageStatus imageStatus) {
    Image image =
        imageRepository.save(
            ImageFixture.builder().memberId(MEMBER_ID).status(imageStatus).build());
    return feedRepository
        .save(
            TextFeedFixture.builder()
                .authorId(member.getId())
                .grade("GOLD")
                .image(feedImage(image, 0))
                .build())
        .getId();
  }

  private long saveOfficialFeed(boolean isPublished) {
    return feedRepository
        .save(
            OfficialFeedFixture.builder()
                .authorId(official.getId())
                .isPublished(isPublished)
                .build())
        .getId();
  }

  private Image saveAdminImage(long adminId) {
    return saveAdminImage(adminId, ImageStatus.COMPLETED);
  }

  private Image saveAdminImage(long adminId, ImageStatus status) {
    return imageRepository.save(ImageFixture.builder().adminId(adminId).status(status).build());
  }

  private static FeedImage feedImage(Image image, int displayOrder) {
    return FeedImage.of(image.getId(), image.getImageKey(), image.getThumbnailKey(), displayOrder);
  }

  private static AdminFeedCreateRequest createRequest(long imageId) {
    return new AdminFeedCreateRequest("공지", "내용", List.of(imageId), null, true, false);
  }

  private long countFeedImageRows(long feedId) {
    return jdbcTemplate.queryForObject(
        "select count(*) from feed_images where feed_id = ?", Long.class, feedId);
  }

  private List<Long> homeFeedIds(String grade) {
    return homeFeedCacheRepository.findSortKeys(grade, Long.MAX_VALUE, 10).stream()
        .map(HomeFeedSortKey::feedId)
        .toList();
  }
}
