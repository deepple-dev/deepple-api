package kr.co.deepple.feed.adapter.out.persistence;

import static kr.co.deepple.feed.domain.QFeedAuthor.feedAuthor;
import static kr.co.deepple.feed.domain.QFeedComment.feedComment;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.FeedCommentWithAuthor;
import kr.co.deepple.feed.application.port.out.FeedCommentQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedCommentQueryRepositoryImpl implements FeedCommentQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<FeedCommentWithAuthor> findTopLevelComments(
      long feedId, Set<Long> blockRelatedMemberIds, Long lastCommentId, int limit) {
    return selectCommentsWithAuthor()
        .where(
            feedComment.feedId.eq(feedId),
            feedComment.parentId.isNull(),
            idLessThan(lastCommentId),
            authorNotIn(blockRelatedMemberIds))
        .orderBy(feedComment.id.desc())
        .limit(limit)
        .fetch();
  }

  @Override
  public List<FeedCommentWithAuthor> findReplies(
      long parentCommentId, Set<Long> blockRelatedMemberIds, Long lastCommentId, int limit) {
    return selectCommentsWithAuthor()
        .where(
            feedComment.parentId.eq(parentCommentId),
            idLessThan(lastCommentId),
            authorNotIn(blockRelatedMemberIds))
        .orderBy(feedComment.id.desc())
        .limit(limit)
        .fetch();
  }

  @Override
  public Map<Long, Long> countReplies(
      Collection<Long> parentCommentIds, Set<Long> blockRelatedMemberIds) {
    if (parentCommentIds.isEmpty()) {
      return Map.of();
    }
    List<Tuple> rows =
        queryFactory
            .select(feedComment.parentId, feedComment.count())
            .from(feedComment)
            .join(feedAuthor)
            .on(feedAuthor.id.eq(feedComment.authorId))
            .where(feedComment.parentId.in(parentCommentIds), authorNotIn(blockRelatedMemberIds))
            .groupBy(feedComment.parentId)
            .fetch();
    return rows.stream()
        .collect(
            Collectors.toMap(
                row -> row.get(feedComment.parentId), row -> row.get(feedComment.count())));
  }

  private JPAQuery<FeedCommentWithAuthor> selectCommentsWithAuthor() {
    return queryFactory
        .select(Projections.constructor(FeedCommentWithAuthor.class, feedComment, feedAuthor))
        .from(feedComment)
        .join(feedAuthor)
        .on(feedAuthor.id.eq(feedComment.authorId));
  }

  private BooleanExpression idLessThan(Long lastCommentId) {
    if (lastCommentId == null) {
      return null;
    }
    return feedComment.id.lt(lastCommentId);
  }

  private BooleanExpression authorNotIn(Set<Long> blockRelatedMemberIds) {
    if (blockRelatedMemberIds.isEmpty()) {
      return null;
    }
    return feedAuthor.memberId.isNull().or(feedAuthor.memberId.notIn(blockRelatedMemberIds));
  }
}
