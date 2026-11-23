package kr.co.deepple.feed.application;

import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;

public record AccessibleFeed(Feed feed, FeedAuthor author, boolean isMine) {

  static AccessibleFeed mine(Feed feed, FeedAuthor me) {
    return new AccessibleFeed(feed, me, true);
  }

  static AccessibleFeed others(Feed feed, FeedAuthor author) {
    return new AccessibleFeed(feed, author, false);
  }
}
