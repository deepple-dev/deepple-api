package kr.co.deepple.feed.domain;

import java.util.Set;
import lombok.NonNull;

public record FeedViewer(long authorId, String grade, @NonNull Set<Long> blockRelatedMemberIds) {

  public static FeedViewer of(FeedAuthor me, Set<Long> blockRelatedMemberIds) {
    return new FeedViewer(me.getId(), me.getGrade(), Set.copyOf(blockRelatedMemberIds));
  }

  public boolean canSeeAuthor(FeedAuthorStatus author) {
    if (author.isOfficial()) {
      return true;
    }
    if (!author.isActive()) {
      return false;
    }
    return !isBlockedWith(author);
  }

  private boolean isBlockedWith(FeedAuthorStatus author) {
    return blockRelatedMemberIds.contains(author.getMemberId());
  }
}
