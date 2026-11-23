package kr.co.deepple.feed.application.dto;

public record FeedCounts(long likeCount, long commentCount) {

  public static FeedCounts ofLikeCount(long likeCount) {
    return new FeedCounts(likeCount, 0L);
  }
}
