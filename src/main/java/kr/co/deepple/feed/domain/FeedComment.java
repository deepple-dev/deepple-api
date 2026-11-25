package kr.co.deepple.feed.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.SoftDeleteBaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "feed_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedComment extends SoftDeleteBaseEntity {

  private static final int MAX_CONTENT_LENGTH = 100;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long feedId;

  private Long parentId;

  private long authorId;

  @Column(nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private FeedCommentStatus status;

  private FeedComment(long feedId, Long parentId, long authorId, String content) {
    this.feedId = feedId;
    this.parentId = parentId;
    this.authorId = authorId;
    this.content = content;
    this.status = FeedCommentStatus.PUBLISHED;
  }

  public static FeedComment create(long feedId, long authorId, @NonNull String content) {
    validateContent(content);
    return new FeedComment(feedId, null, authorId, content);
  }

  public static FeedComment reply(
      @NonNull FeedComment parent, long authorId, @NonNull String content) {
    validateTopLevel(parent);
    validateContent(content);
    return new FeedComment(parent.getFeedId(), parent.getId(), authorId, content);
  }

  public boolean isReply() {
    return parentId != null;
  }

  public boolean isWrittenBy(long authorId) {
    return this.authorId == authorId;
  }

  public boolean isHiddenByReport() {
    return status == FeedCommentStatus.HIDDEN_BY_REPORT;
  }

  public void hideByReport() {
    this.status = FeedCommentStatus.HIDDEN_BY_REPORT;
  }

  private static void validateTopLevel(FeedComment parent) {
    if (parent.isReply()) {
      throw invalidParentCommentException(parent);
    }
  }

  private static void validateContent(String content) {
    if (content.isBlank() || content.length() > MAX_CONTENT_LENGTH) {
      throw invalidContentException(content);
    }
  }

  private static DeeppleException invalidParentCommentException(FeedComment parent) {
    return new DeeppleException(
        FeedErrorCode.INVALID_PARENT_COMMENT, "parentCommentId=" + parent.getId());
  }

  private static DeeppleException invalidContentException(String content) {
    return new DeeppleException(
        FeedErrorCode.INVALID_FEED_COMMENT_CONTENT, "length=" + content.length());
  }
}
