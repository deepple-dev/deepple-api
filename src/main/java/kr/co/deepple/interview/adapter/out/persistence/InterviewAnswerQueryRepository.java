package kr.co.deepple.interview.adapter.out.persistence;

import static kr.co.deepple.interview.domain.QInterviewAnswer.interviewAnswer;
import static kr.co.deepple.interview.domain.QInterviewQuestion.interviewQuestion;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterviewAnswerQueryRepository {

  private final JPAQueryFactory queryFactory;

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
}
