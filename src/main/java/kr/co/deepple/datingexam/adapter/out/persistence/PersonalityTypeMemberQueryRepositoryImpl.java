package kr.co.deepple.datingexam.adapter.out.persistence;

import static kr.co.deepple.datingexam.domain.QDatingExamSubmitResult.datingExamSubmitResult;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.datingexam.application.port.out.PersonalityTypeMemberQueryRepository;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PersonalityTypeMemberQueryRepositoryImpl
    implements PersonalityTypeMemberQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Set<Long> findMemberIdsByDominantType(AnswerPersonalityType type) {
    return new HashSet<>(
        queryFactory
            .select(datingExamSubmitResult.memberId)
            .from(datingExamSubmitResult)
            .where(datingExamSubmitResult.dominantPersonalityType.eq(type))
            .fetch());
  }
}
