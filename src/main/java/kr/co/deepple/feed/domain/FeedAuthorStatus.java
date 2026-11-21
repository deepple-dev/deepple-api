package kr.co.deepple.feed.domain;

public interface FeedAuthorStatus {

  Long getMemberId();

  boolean isOfficial();

  boolean isActive();
}
