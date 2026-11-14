package kr.co.deepple.interview.adapter.out.persistence;

import static kr.co.deepple.interview.domain.QInterviewAnswer.interviewAnswer;
import static kr.co.deepple.interview.domain.QInterviewQuestion.interviewQuestion;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswer;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswerDetail;
import kr.co.deepple.interview.application.port.out.InterviewAnswerQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterviewAnswerQueryRepositoryImpl implements InterviewAnswerQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<MemberInterviewAnswer> findPublicAnswersByMemberId(long memberId) {
    return queryFactory
        .select(
            Projections.constructor(
                MemberInterviewAnswer.class,
                interviewQuestion.id,
                interviewQuestion.content,
                interviewQuestion.category.stringValue(),
                interviewAnswer.id,
                interviewAnswer.content))
        .from(interviewAnswer)
        .join(interviewQuestion)
        .on(interviewAnswer.questionId.eq(interviewQuestion.id))
        .where(interviewQuestion.isPublic.isTrue(), interviewAnswer.memberId.eq(memberId))
        .orderBy(interviewAnswer.id.asc())
        .fetch();
  }

  @Override
  public List<MemberInterviewAnswerDetail> findAllAnswersByMemberId(long memberId) {
    return queryFactory
        .select(
            interviewAnswer.id,
            interviewQuestion.id,
            interviewQuestion.content,
            interviewQuestion.isPublic,
            interviewAnswer.content)
        .from(interviewAnswer)
        .leftJoin(interviewQuestion)
        .on(interviewAnswer.questionId.eq(interviewQuestion.id))
        .where(interviewAnswer.memberId.eq(memberId))
        .orderBy(interviewAnswer.id.asc())
        .fetch()
        .stream()
        .map(tuple ->
            new MemberInterviewAnswerDetail(
                tuple.get(interviewAnswer.id),
                tuple.get(interviewQuestion.id),
                tuple.get(interviewQuestion.content),
                Boolean.TRUE.equals(tuple.get(interviewQuestion.isPublic)),
                tuple.get(interviewAnswer.content)))
        .toList();
  }
}
