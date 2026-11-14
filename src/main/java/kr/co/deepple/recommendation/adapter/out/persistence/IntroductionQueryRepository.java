package kr.co.deepple.recommendation.adapter.out.persistence;

import static kr.co.deepple.recommendation.domain.QMemberIntroduction.memberIntroduction;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IntroductionQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Set<Long> findAllIntroducedMemberId(long memberId) {
    return new HashSet<>(
        queryFactory
            .select(memberIntroduction.introducedMemberId)
            .from(memberIntroduction)
            .where(memberIntroduction.memberId.eq(memberId))
            .fetch());
  }

  public Set<Long> findIntroducedMemberIds(long memberId, Collection<Long> memberIds) {
    if (memberIds.isEmpty()) {
      return Set.of();
    }
    return new HashSet<>(
        queryFactory
            .select(memberIntroduction.introducedMemberId)
            .from(memberIntroduction)
            .where(
                memberIntroduction.memberId.eq(memberId),
                memberIntroduction.introducedMemberId.in(memberIds))
            .fetch());
  }
}
