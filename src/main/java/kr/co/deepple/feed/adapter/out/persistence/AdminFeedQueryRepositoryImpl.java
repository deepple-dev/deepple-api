package kr.co.deepple.feed.adapter.out.persistence;

import static kr.co.deepple.feed.domain.QOfficialFeed.officialFeed;
import static kr.co.deepple.feed.domain.QUserFeed.userFeed;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.co.deepple.feed.application.port.out.AdminFeedQueryRepository;
import kr.co.deepple.feed.domain.FeedStatus;
import kr.co.deepple.feed.domain.OfficialFeed;
import kr.co.deepple.feed.domain.UserFeed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminFeedQueryRepositoryImpl implements AdminFeedQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<UserFeed> findUserFeeds(FeedStatus status, Pageable pageable) {
    BooleanExpression[] conditions = {
      userFeed.deletedAt.isNull(), statusEq(userFeed.status, status)
    };
    List<UserFeed> feeds =
        queryFactory
            .selectFrom(userFeed)
            .where(conditions)
            .orderBy(userFeed.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    long totalCount =
        queryFactory.select(userFeed.count()).from(userFeed).where(conditions).fetchOne();
    return new PageImpl<>(feeds, pageable, totalCount);
  }

  @Override
  public Page<OfficialFeed> findOfficialFeeds(FeedStatus status, Pageable pageable) {
    BooleanExpression[] conditions = {
      officialFeed.deletedAt.isNull(), statusEq(officialFeed.status, status)
    };
    List<OfficialFeed> feeds =
        queryFactory
            .selectFrom(officialFeed)
            .where(conditions)
            .orderBy(officialFeed.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    long totalCount =
        queryFactory.select(officialFeed.count()).from(officialFeed).where(conditions).fetchOne();
    return new PageImpl<>(feeds, pageable, totalCount);
  }

  private BooleanExpression statusEq(EnumPath<FeedStatus> path, FeedStatus status) {
    return status != null ? path.eq(status) : null;
  }
}
