package kr.co.deepple.feed.application.dto;

import java.util.List;

public record FeedCommentPage(List<FeedCommentWithAuthor> comments, boolean hasMore) {

  public static FeedCommentPage of(List<FeedCommentWithAuthor> fetched, int pageSize) {
    if (fetched.size() <= pageSize) {
      return new FeedCommentPage(fetched, false);
    }
    return new FeedCommentPage(fetched.subList(0, pageSize), true);
  }

  public List<Long> commentIds() {
    return comments.stream().map(comment -> comment.comment().getId()).toList();
  }

  public String nextCursor() {
    if (!hasMore) {
      return null;
    }
    return FeedCommentCursor.of(comments.getLast().comment()).toCursor();
  }
}
