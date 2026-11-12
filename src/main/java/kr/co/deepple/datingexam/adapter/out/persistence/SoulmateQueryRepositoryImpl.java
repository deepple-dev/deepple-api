package kr.co.deepple.datingexam.adapter.out.persistence;

import static kr.co.deepple.block.domain.QBlock.block;
import static kr.co.deepple.datingexam.domain.QDatingExamSubject.datingExamSubject;
import static kr.co.deepple.datingexam.domain.QDatingExamSubmit.datingExamSubmit;
import static kr.co.deepple.member.command.domain.member.QMember.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.port.out.SoulmateQueryRepository;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import kr.co.deepple.datingexam.domain.SubjectType;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SoulmateQueryRepositoryImpl implements SoulmateQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Set<Long> findSameAnswerMemberIds(Long memberId) {
    Set<Long> equalAnswerMemberIds = getEqualAnswerMemberIds(memberId);
    Set<Long> excludedMemberIds = getExcludedMemberIds(memberId);

    equalAnswerMemberIds.removeAll(excludedMemberIds);

    if (equalAnswerMemberIds.isEmpty()) {
      return Collections.emptySet();
    }

    Gender gender =
        queryFactory
            .select(member.profile.gender)
            .from(member)
            .where(member.id.eq(memberId))
            .fetchOne();

    if (gender == null) {
      throw new DeeppleException(DatingExamErrorCode.GENDER_NOT_SET, "memberId=" + memberId);
    }

    return queryFactory
        .select(member.id)
        .from(member)
        .where(
            member
                .id
                .in(equalAnswerMemberIds)
                .and(member.profile.gender.eq(gender.getOpposite()))
                .and(member.isProfilePublic.isTrue())
                .and(member.activityStatus.eq(ActivityStatus.ACTIVE))
                .and(member.id.ne(memberId)))
        .orderBy(member.id.desc())
        .fetch()
        .stream()
        .collect(Collectors.toSet());
  }

  private Set<Long> getExcludedMemberIds(Long memberId) {
    List<Long> blockedIds =
        queryFactory
            .select(block.blockedId)
            .from(block)
            .where(block.blockerId.eq(memberId))
            .fetch();

    List<Long> blockerIds =
        queryFactory
            .select(block.blockerId)
            .from(block)
            .where(block.blockedId.eq(memberId))
            .fetch();

    return Stream.concat(
            Stream.concat(blockedIds.stream(), blockerIds.stream()), Stream.of(memberId))
        .collect(Collectors.toSet());
  }

  private Set<Long> getEqualAnswerMemberIds(Long memberId) {
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
