package kr.co.deepple.feed.adapter.in.web;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import kr.co.deepple.feed.application.dto.AdminFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminFeedView;
import kr.co.deepple.feed.application.dto.FeedAuthorView;
import kr.co.deepple.feed.application.dto.FeedBackgroundView;
import kr.co.deepple.feed.application.dto.FeedBackgroundViews;
import kr.co.deepple.feed.application.dto.FeedCommentDisplayStatus;
import kr.co.deepple.feed.application.dto.FeedCommentView;
import kr.co.deepple.feed.application.dto.FeedCommentViews;
import kr.co.deepple.feed.application.dto.FeedGridItemView;
import kr.co.deepple.feed.application.dto.FeedGridViews;
import kr.co.deepple.feed.application.dto.FeedImageView;
import kr.co.deepple.feed.application.dto.FeedPollOptionView;
import kr.co.deepple.feed.application.dto.FeedPollView;
import kr.co.deepple.feed.application.dto.FeedSearchCondition;
import kr.co.deepple.feed.application.dto.FeedTextPosition;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.dto.FeedViews;
import kr.co.deepple.feed.application.dto.ImageUploadResponse;
import kr.co.deepple.feed.domain.FeedAuthorType;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.FeedType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FeedSamples {

  private static final String IMAGE_HOST = "https://example.com/feed";
  private static final String SECOND_PAGE_CURSOR = "cursor-2";
  private static final int BACKGROUND_COUNT = 9;

  private static final FeedTextPosition CENTER = new FeedTextPosition(0.5, 0.5, 0.0, 1.0);
  private static final FeedTextPosition TOP_LEFT = new FeedTextPosition(0.2, 0.15, 0.0, 1.0);
  private static final FeedTextPosition BOTTOM_RIGHT = new FeedTextPosition(0.8, 0.85, 0.0, 1.0);

  private static final String TEXT_100 = "오늘은 날씨가 정말 좋아서 한강에 다녀왔어요. ".repeat(5).substring(0, 100);
  private static final String TEXT_200 =
      "11월 한 달 동안 매일 접속하면 하트를 두 배로 드려요. ".repeat(8).substring(0, 200);
  private static final String MULTILINE_TEXT = "첫 줄이에요\n둘째 줄이에요\n\n한 줄 띄고 넷째 줄 🙂🎉";
  private static final String QUESTION_19 = "주말 데이트 장소 어디가 좋을까요?";
  private static final String OPTION_20 = "분위기 좋은 루프탑 레스토랑에서 저녁";
  private static final String TITLE_50 = "관리자 피드 타이틀 ".repeat(5).substring(0, 50);

  private static final Map<Integer, List<Integer>> VOTE_PERCENTS =
      Map.of(2, List.of(67, 33), 3, List.of(50, 30, 20), 4, List.of(40, 30, 20, 10));

  private static final FeedAuthorView GINGER =
      member(101L, "진저", "성장하는 러닝메이트", "growing-running-mate", 29, "FEMALE", "SEOUL", "ENFP");
  private static final FeedAuthorView RUNNER =
      member(102L, "한강러너", "단호한 독립주의자", "decisive-independent", 33, "MALE", "GYEONGGI", "ISTJ");
  private static final FeedAuthorView LONG_NICKNAME =
      member(103L, "열글자닉네임쓰는사람", "헌신적인 로맨티스트", "devoted-romantic", 41, "MALE", "BUSAN", "INFP");
  private static final FeedAuthorView ME =
      member(1L, "나", "현실적인 안식처", "realistic-shelter", 30, "FEMALE", "SEOUL", "ISFJ");
  private static final FeedAuthorView OFFICIAL =
      new FeedAuthorView(
          FeedAuthorType.OFFICIAL.name(),
          null,
          "deepple_official",
          null,
          null,
          IMAGE_HOST + "/profiles/official.webp",
          null,
          null,
          null,
          null);
  private static final List<FeedAuthorView> MEMBERS = List.of(GINGER, RUNNER, LONG_NICKNAME);

  static FeedViews feeds(FeedSearchCondition condition) {
    if (FeedAuthorType.OFFICIAL.name().equals(condition.authorType())) {
      return new FeedViews(officialFeeds(), null, false);
    }
    if (SECOND_PAGE_CURSOR.equals(condition.cursor())) {
      return new FeedViews(simpleFeeds(21L, 25L), null, false);
    }
    return new FeedViews(firstPageFeeds(), SECOND_PAGE_CURSOR, true);
  }

  static FeedGridViews myFeedGrid() {
    return new FeedGridViews(myFeeds().stream().map(FeedSamples::gridItem).toList(), null, false);
  }

  static FeedView feed(long feedId) {
    return Stream.of(firstPageFeeds(), simpleFeeds(21L, 25L), myFeeds())
        .flatMap(List::stream)
        .filter(feed -> feed.feedId() == feedId)
        .findFirst()
        .orElseGet(() -> firstPageFeeds().get(2));
  }

  static FeedView createdFeed() {
    return myFeeds().get(0);
  }

  static FeedPollView vote(long feedId, long optionId) {
    FeedPollView poll = feed(feedId).poll();
    if (poll == null) {
      poll = feed(7L).poll();
    }
    return withResult(poll, optionId);
  }

  static FeedBackgroundViews backgrounds() {
    return new FeedBackgroundViews(
        LongStream.rangeClosed(1L, BACKGROUND_COUNT).mapToObj(FeedSamples::background).toList());
  }

  static FeedCommentViews comments(String cursor) {
    if (SECOND_PAGE_CURSOR.equals(cursor)) {
      return new FeedCommentViews(simpleComments(21L, 23L), null, false);
    }
    List<FeedCommentView> comments = new ArrayList<>();
    comments.add(comment(1L, null, GINGER, "저도 가고 싶어요!", 1, false, 60 * 24 * 40));
    comments.add(comment(2L, null, RUNNER, TEXT_100, 25, false, 60 * 24 * 3));
    comments.add(comment(3L, null, LONG_NICKNAME, MULTILINE_TEXT, 0, false, 60 * 23));
    comments.add(comment(4L, null, ME, "내가 쓴 댓글이에요", 0, true, 60 * 5));
    comments.add(hiddenComment(5L, FeedCommentDisplayStatus.DELETED, 2));
    comments.add(hiddenComment(6L, FeedCommentDisplayStatus.HIDDEN, 0));
    comments.add(hiddenComment(7L, FeedCommentDisplayStatus.INACTIVE_AUTHOR, 0));
    comments.addAll(simpleComments(8L, 20L));
    return new FeedCommentViews(comments, SECOND_PAGE_CURSOR, true);
  }

  static FeedCommentViews replies(long commentId) {
    return new FeedCommentViews(
        List.of(
            comment(101L, commentId, RUNNER, "같이 가요", 0, false, 60 * 24 * 40),
            comment(102L, commentId, ME, "내가 쓴 답글이에요", 0, true, 60 * 24 * 3),
            comment(103L, commentId, GINGER, TEXT_100, 0, false, 30),
            new FeedCommentView(
                104L,
                commentId,
                FeedCommentDisplayStatus.DELETED.name(),
                null,
                null,
                0,
                false,
                minutesAgo(1))),
        null,
        false);
  }

  static FeedCommentView createdComment(Long parentCommentId) {
    return comment(200L, parentCommentId, ME, "방금 쓴 댓글이에요", 0, true, 0);
  }

  static ImageUploadResponse imageUpload() {
    return new ImageUploadResponse(
        10L,
        "https://example-bucket.s3.ap-northeast-2.amazonaws.com",
        Map.of(
            "key", "tmp/1/0b7c1f5e.webp",
            "Content-Type", "image/webp",
            "policy", "example-policy",
            "x-amz-algorithm", "AWS4-HMAC-SHA256",
            "x-amz-credential", "example-credential",
            "x-amz-date", "20261114T030000Z",
            "x-amz-signature", "example-signature"));
  }

  static List<AdminFeedView> adminFeeds(AdminFeedSearchCondition condition) {
    return Stream.of(
            adminFeed(1L, FeedType.OFFICIAL, FeedStatus.PUBLISHED, "11월 이벤트 안내", TEXT_200, 5, true),
            adminFeed(2L, FeedType.OFFICIAL, FeedStatus.HIDDEN, TITLE_50, "공지", 0, false),
            adminFeed(6L, FeedType.TEXT, FeedStatus.PENDING, null, "오늘 다녀온 전시 사진이에요", 1, false),
            adminFeed(9L, FeedType.POLL, FeedStatus.PENDING, null, "여행은 어디로?", 1, false),
            adminFeed(41L, FeedType.TEXT, FeedStatus.REJECTED, null, TEXT_100, 1, false),
            adminFeed(42L, FeedType.TEXT, FeedStatus.HIDDEN_BY_REPORT, null, "신고로 숨겨진 글", 0, false),
            adminFeed(43L, FeedType.TEXT, FeedStatus.PUBLISHED, null, "승인된 사진 피드", 1, false))
        .filter(feed -> condition.status() == null || condition.status().equals(feed.status()))
        .filter(
            feed -> condition.authorType() == null || isAuthorType(feed, condition.authorType()))
        .toList();
  }

  static AdminFeedView adminOfficialFeed(long feedId) {
    return adminFeed(
        feedId, FeedType.OFFICIAL, FeedStatus.PUBLISHED, "11월 이벤트 안내", TEXT_200, 5, true);
  }

  private static List<FeedView> firstPageFeeds() {
    List<FeedView> feeds = new ArrayList<>(officialFeeds());
    feeds.add(userFeed(3L, FeedType.TEXT, GINGER, "안녕", CENTER, 1L, null, null, 0L, false, 0L));
    feeds.add(
        userFeed(4L, FeedType.TEXT, RUNNER, TEXT_100, CENTER, 2L, null, null, 999L, true, 120L));
    feeds.add(
        userFeed(
            5L,
            FeedType.TEXT,
            LONG_NICKNAME,
            MULTILINE_TEXT,
            TOP_LEFT,
            3L,
            null,
            null,
            3L,
            false,
            1L));
    feeds.add(
        userFeed(
            6L,
            FeedType.TEXT,
            GINGER,
            "오늘 다녀온 전시 사진이에요",
            BOTTOM_RIGHT,
            null,
            10L,
            null,
            15L,
            false,
            2L));
    feeds.add(
        userFeed(
            7L,
            FeedType.POLL,
            RUNNER,
            "짜장면 vs 짬뽕",
            CENTER,
            4L,
            null,
            poll(71L, "짜장면", "짬뽕"),
            4L,
            false,
            0L));
    feeds.add(
        userFeed(
            8L,
            FeedType.POLL,
            LONG_NICKNAME,
            QUESTION_19,
            CENTER,
            5L,
            null,
            withResult(poll(81L, OPTION_20, "한강 피크닉", "영화관", "집"), 82L),
            88L,
            true,
            40L));
    feeds.add(
        userFeed(
            9L,
            FeedType.POLL,
            GINGER,
            "여행은 어디로?",
            TOP_LEFT,
            null,
            11L,
            poll(91L, "제주도", "부산", "강릉"),
            1L,
            false,
            0L));
    feeds.addAll(simpleFeeds(10L, 20L));
    return feeds;
  }

  private static List<FeedView> officialFeeds() {
    return List.of(
        new FeedView(
            1L,
            FeedType.OFFICIAL.name(),
            OFFICIAL,
            TEXT_200,
            null,
            null,
            images(20L, 5),
            null,
            true,
            12345L,
            true,
            230L,
            false,
            minutesAgo(60 * 24 * 40)),
        new FeedView(
            2L,
            FeedType.OFFICIAL.name(),
            OFFICIAL,
            "공지",
            null,
            null,
            images(25L, 1),
            null,
            true,
            0L,
            false,
            0L,
            false,
            minutesAgo(60 * 24 * 3)));
  }

  private static List<FeedView> myFeeds() {
    return List.of(
        myFeed(31L, FeedType.TEXT, "내가 쓴 짧은 글", CENTER, 1L, null, null),
        myFeed(32L, FeedType.TEXT, TEXT_100, TOP_LEFT, 9L, null, null),
        myFeed(33L, FeedType.TEXT, "심사 중이거나 반려된 사진 피드도 똑같이 보여요", CENTER, null, 12L, null),
        myFeed(
            34L,
            FeedType.POLL,
            "내가 올린 투표",
            CENTER,
            6L,
            null,
            withResult(poll(341L, "첫째", "둘째", "셋째"), null)),
        myFeed(
            35L,
            FeedType.POLL,
            QUESTION_19,
            BOTTOM_RIGHT,
            null,
            13L,
            withResult(poll(351L, OPTION_20, "집"), null)));
  }

  private static List<FeedView> simpleFeeds(long firstFeedId, long lastFeedId) {
    return LongStream.rangeClosed(firstFeedId, lastFeedId)
        .mapToObj(feedId ->
            userFeed(
                feedId,
                FeedType.TEXT,
                MEMBERS.get((int) (feedId % MEMBERS.size())),
                "피드 " + feedId + "번이에요",
                CENTER,
                feedId % BACKGROUND_COUNT + 1L,
                null,
                null,
                feedId,
                feedId % 2 == 0,
                feedId % 4))
        .toList();
  }

  private static List<FeedCommentView> simpleComments(long firstCommentId, long lastCommentId) {
    return LongStream.rangeClosed(firstCommentId, lastCommentId)
        .mapToObj(commentId ->
            comment(
                commentId,
                null,
                MEMBERS.get((int) (commentId % MEMBERS.size())),
                "댓글 " + commentId + "번이에요",
                0,
                false,
                60 - commentId * 2))
        .toList();
  }

  private static FeedView userFeed(
      long feedId,
      FeedType type,
      FeedAuthorView author,
      String content,
      FeedTextPosition textPosition,
      Long backgroundId,
      Long imageId,
      FeedPollView poll,
      long likeCount,
      boolean isLiked,
      long commentCount) {
    return new FeedView(
        feedId,
        type.name(),
        author,
        content,
        textPosition,
        backgroundId == null ? null : background(backgroundId),
        imageId == null ? List.of() : images(imageId, 1),
        poll,
        false,
        likeCount,
        isLiked,
        commentCount,
        author == ME,
        minutesAgo(feedId * feedId * 10));
  }

  private static FeedView myFeed(
      long feedId,
      FeedType type,
      String content,
      FeedTextPosition textPosition,
      Long backgroundId,
      Long imageId,
      FeedPollView poll) {
    return userFeed(
        feedId, type, ME, content, textPosition, backgroundId, imageId, poll, 2L, false, 1L);
  }

  private static FeedGridItemView gridItem(FeedView feed) {
    String thumbnailUrl =
        feed.images().isEmpty()
            ? feed.background().thumbnailUrl()
            : feed.images().get(0).thumbnailUrl();
    return new FeedGridItemView(
        feed.feedId(), feed.type(), thumbnailUrl, feed.content(), feed.textPosition());
  }

  private static FeedPollView poll(long firstOptionId, String... contents) {
    List<FeedPollOptionView> options = new ArrayList<>();
    for (int index = 0; index < contents.length; index++) {
      options.add(new FeedPollOptionView(firstOptionId + index, contents[index], null));
    }
    return new FeedPollView(options, null, false);
  }

  private static FeedPollView withResult(FeedPollView poll, Long myOptionId) {
    List<Integer> percents = VOTE_PERCENTS.get(poll.options().size());
    List<FeedPollOptionView> options = new ArrayList<>();
    for (int index = 0; index < poll.options().size(); index++) {
      FeedPollOptionView option = poll.options().get(index);
      options.add(new FeedPollOptionView(option.optionId(), option.content(), percents.get(index)));
    }
    return new FeedPollView(options, myOptionId, true);
  }

  private static FeedCommentView comment(
      long commentId,
      Long parentCommentId,
      FeedAuthorView author,
      String content,
      int replyCount,
      boolean isMine,
      long minutesAgo) {
    return new FeedCommentView(
        commentId,
        parentCommentId,
        FeedCommentDisplayStatus.VISIBLE.name(),
        author,
        content,
        replyCount,
        isMine,
        minutesAgo(minutesAgo));
  }

  private static FeedCommentView hiddenComment(
      long commentId, FeedCommentDisplayStatus status, int replyCount) {
    return new FeedCommentView(
        commentId, null, status.name(), null, null, replyCount, false, minutesAgo(60 * 2));
  }

  private static AdminFeedView adminFeed(
      long feedId,
      FeedType type,
      FeedStatus status,
      String title,
      String content,
      int imageCount,
      boolean isPinned) {
    return new AdminFeedView(
        feedId,
        type.name(),
        status.name(),
        title,
        type == FeedType.OFFICIAL ? OFFICIAL.nickname() : GINGER.nickname(),
        content,
        images(feedId * 100, imageCount),
        isPinned,
        minutesAgo(60 * 24 * feedId),
        status == FeedStatus.PENDING || status == FeedStatus.REJECTED
            ? null
            : minutesAgo(60 * 23 * feedId));
  }

  private static boolean isAuthorType(AdminFeedView feed, String authorType) {
    boolean isOfficial = FeedType.OFFICIAL.name().equals(feed.type());
    return FeedAuthorType.OFFICIAL.name().equals(authorType) == isOfficial;
  }

  private static FeedAuthorView member(
      long memberId,
      String nickname,
      String personalityTypeName,
      String character,
      int age,
      String gender,
      String city,
      String mbti) {
    return new FeedAuthorView(
        FeedAuthorType.MEMBER.name(),
        memberId,
        nickname,
        personalityTypeName,
        IMAGE_HOST + "/characters/" + character + ".webp",
        IMAGE_HOST + "/profiles/" + memberId + ".webp",
        age,
        gender,
        city,
        mbti);
  }

  private static FeedBackgroundView background(long backgroundId) {
    String path = IMAGE_HOST + "/backgrounds/v1/" + backgroundId;
    return new FeedBackgroundView(backgroundId, path + ".webp", path + "-thumbnail.webp");
  }

  private static List<FeedImageView> images(long firstImageId, int count) {
    return LongStream.range(firstImageId, firstImageId + count)
        .mapToObj(imageId -> {
          String path = IMAGE_HOST + "/images/" + imageId;
          return new FeedImageView(imageId, path + ".webp", path + "-thumbnail.webp");
        })
        .toList();
  }

  private static LocalDateTime minutesAgo(long minutes) {
    return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusMinutes(minutes);
  }
}
