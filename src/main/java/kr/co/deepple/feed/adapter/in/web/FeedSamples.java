package kr.co.deepple.feed.adapter.in.web;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import kr.co.deepple.feed.application.dto.FeedAuthorView;
import kr.co.deepple.feed.application.dto.FeedCommentDisplayStatus;
import kr.co.deepple.feed.application.dto.FeedCommentView;
import kr.co.deepple.feed.application.dto.FeedCommentViews;
import kr.co.deepple.feed.domain.FeedAuthorType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FeedSamples {

  private static final String IMAGE_HOST = "https://example.com/feed";
  private static final String SECOND_PAGE_CURSOR = "cursor-2";

  private static final String TEXT_100 = "오늘은 날씨가 정말 좋아서 한강에 다녀왔어요. ".repeat(5).substring(0, 100);
  private static final String MULTILINE_TEXT = "첫 줄이에요\n둘째 줄이에요\n\n한 줄 띄고 넷째 줄 🙂🎉";

  private static final FeedAuthorView GINGER =
      member(101L, "진저", "GROWING_RUNNING_MATE", "동반성장형", 29, "FEMALE", "SEOUL", "ENFP");
  private static final FeedAuthorView RUNNER =
      member(102L, "한강러너", "DECISIVE_INDEPENDENT", "자기주도형", 33, "MALE", "GYEONGGI", "ISTJ");
  private static final FeedAuthorView LONG_NICKNAME =
      member(103L, "열글자닉네임쓰는사람", "DEVOTED_ROMANTIC", "정서교감형", 41, "MALE", "BUSAN", "INFP");
  private static final FeedAuthorView ME =
      member(1L, "나", "REALISTIC_SHELTER", "현실안정형", 30, "FEMALE", "SEOUL", "ISFJ");
  private static final List<FeedAuthorView> MEMBERS = List.of(GINGER, RUNNER, LONG_NICKNAME);

  static FeedCommentViews comments(String cursor) {
    if (SECOND_PAGE_CURSOR.equals(cursor)) {
      return new FeedCommentViews(simpleComments(3L, 1L), null, false);
    }
    List<FeedCommentView> comments = new ArrayList<>();
    comments.add(comment(23L, null, ME, "내가 쓴 댓글이에요", 0, true, 5));
    comments.add(comment(22L, null, LONG_NICKNAME, MULTILINE_TEXT, 0, false, 60 * 5));
    comments.add(comment(21L, null, RUNNER, TEXT_100, 25, false, 60 * 23));
    comments.add(comment(20L, null, GINGER, "저도 가고 싶어요!", 1, false, 60 * 24 * 3));
    comments.add(hiddenComment(19L, FeedCommentDisplayStatus.DELETED, 2, 60 * 24 * 4));
    comments.add(hiddenComment(18L, FeedCommentDisplayStatus.HIDDEN, 0, 60 * 24 * 5));
    comments.add(hiddenComment(17L, FeedCommentDisplayStatus.INACTIVE_AUTHOR, 0, 60 * 24 * 6));
    comments.addAll(simpleComments(16L, 4L));
    return new FeedCommentViews(comments, SECOND_PAGE_CURSOR, true);
  }

  static FeedCommentViews replies(long commentId) {
    return new FeedCommentViews(
        List.of(
            new FeedCommentView(
                104L,
                commentId,
                FeedCommentDisplayStatus.DELETED.name(),
                null,
                null,
                0,
                false,
                minutesAgo(1)),
            comment(103L, commentId, GINGER, TEXT_100, 0, false, 30),
            comment(102L, commentId, ME, "내가 쓴 답글이에요", 0, true, 60 * 24 * 3),
            comment(101L, commentId, RUNNER, "같이 가요", 0, false, 60 * 24 * 40)),
        null,
        false);
  }

  static FeedCommentView createdComment(Long parentCommentId) {
    return comment(200L, parentCommentId, ME, "방금 쓴 댓글이에요", 0, true, 0);
  }

  private static List<FeedCommentView> simpleComments(long newestCommentId, long oldestCommentId) {
    return LongStream.iterate(
            newestCommentId, commentId -> commentId >= oldestCommentId, commentId -> commentId - 1L)
        .mapToObj(commentId ->
            comment(
                commentId,
                null,
                MEMBERS.get((int) (commentId % MEMBERS.size())),
                "댓글 " + commentId + "번이에요",
                0,
                false,
                60 * 24 * (40L - commentId * 2L)))
        .toList();
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
      long commentId, FeedCommentDisplayStatus status, int replyCount, long minutesAgo) {
    return new FeedCommentView(
        commentId, null, status.name(), null, null, replyCount, false, minutesAgo(minutesAgo));
  }

  private static FeedAuthorView member(
      long memberId,
      String nickname,
      String personalityType,
      String personalityTypeName,
      int age,
      String gender,
      String city,
      String mbti) {
    return new FeedAuthorView(
        FeedAuthorType.MEMBER.name(),
        memberId,
        nickname,
        personalityType,
        personalityTypeName,
        IMAGE_HOST + "/profiles/" + memberId + ".webp",
        age,
        gender,
        city,
        mbti);
  }

  private static LocalDateTime minutesAgo(long minutes) {
    return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusMinutes(minutes);
  }
}
