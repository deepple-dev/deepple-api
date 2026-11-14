package kr.co.deepple.datingexam.adapter.out.persistence;

import static kr.co.deepple.datingexam.domain.QDatingExamSubject.datingExamSubject;
import static kr.co.deepple.datingexam.domain.QDatingExamSubmit.datingExamSubmit;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.port.out.SoulmateQueryRepository;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import kr.co.deepple.datingexam.domain.SubjectType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SoulmateQueryRepositoryImpl implements SoulmateQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Set<Long> findSameAnswerMemberIds(long memberId) {
    return getEqualAnswerMemberIds(memberId);
  }

  private Set<Long> getEqualAnswerMemberIds(long memberId) {
    List<DatingExamSubmit> submits =
        queryFactory
            .select(datingExamSubmit)
            .from(datingExamSubmit)
            .innerJoin(datingExamSubject)
            .on(
                datingExamSubmit.subjectId.eq(datingExamSubject.id),
                datingExamSubject.type.eq(SubjectType.REQUIRED))
            .where(datingExamSubmit.memberId.eq(memberId))
            .fetch();

    if (submits.isEmpty()) {
      throw new DeeppleException(
          DatingExamErrorCode.DATING_EXAM_RESULT_NOT_FOUND, "memberId=" + memberId);
    }

    List<Set<Long>> equalAnswerMemberIdSets =
        submits.stream()
            .map(submit ->
                new HashSet<>(
                        queryFactory
                            .select(datingExamSubmit.memberId)
                            .from(datingExamSubmit)
                            .where(
                                datingExamSubmit
                                    .subjectId
                                    .eq(submit.getSubjectId())
                                    .and(datingExamSubmit.answers.eq(submit.getAnswers())))
                            .fetch())
                    .stream().collect(Collectors.toSet()))
            .toList();

    return equalAnswerMemberIdSets.stream()
        .sorted(Comparator.comparingInt(Set::size))
        .reduce((a, b) -> {
          a.retainAll(b);
          return a;
        })
        .orElseGet(Collections::emptySet);
  }
}
