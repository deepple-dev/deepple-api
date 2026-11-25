package kr.co.deepple.feed.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.feed.application.dto.FeedCommentWithAuthor;

public interface FeedCommentQueryRepository {

  List<FeedCommentWithAuthor> findTopLevelComments(
      long feedId, Set<Long> blockRelatedMemberIds, Long lastCommentId, int limit);

  List<FeedCommentWithAuthor> findReplies(
      long parentCommentId, Set<Long> blockRelatedMemberIds, Long lastCommentId, int limit);

  Map<Long, Long> countReplies(Collection<Long> parentCommentIds, Set<Long> blockRelatedMemberIds);
}
