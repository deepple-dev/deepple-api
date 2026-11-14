package kr.co.deepple.recommendation.adapter.out.persistence;

import static kr.co.deepple.recommendation.domain.QMemberIntroduction.memberIntroduction;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.recommendation.application.port.out.IntroductionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IntroductionQueryRepositoryImpl implements IntroductionQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Set<Long> findAllIntroducedMemberId(long memberId) {
    return new HashSet<>(
        queryFactory
            .select(memberIntroduction.introducedMemberId)
            .from(memberIntroduction)
            .where(memberIntroduction.memberId.eq(memberId))
            .fetch());
  }

  @Override
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
