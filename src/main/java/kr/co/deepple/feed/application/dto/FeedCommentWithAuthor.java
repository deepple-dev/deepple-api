package kr.co.deepple.feed.application.dto;

import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;

public record FeedCommentWithAuthor(FeedComment comment, FeedAuthor author) {}
