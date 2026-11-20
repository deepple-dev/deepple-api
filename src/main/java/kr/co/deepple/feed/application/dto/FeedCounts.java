package kr.co.deepple.feed.application.dto;

public record FeedCounts(long likeCount, long commentCount) {

  public static final FeedCounts EMPTY = new FeedCounts(0L, 0L);
}
