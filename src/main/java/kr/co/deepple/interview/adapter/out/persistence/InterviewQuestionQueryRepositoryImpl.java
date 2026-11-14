package kr.co.deepple.interview.adapter.out.persistence;

import static kr.co.deepple.interview.domain.QInterviewAnswer.interviewAnswer;
import static kr.co.deepple.interview.domain.QInterviewQuestion.interviewQuestion;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.interview.application.dto.InterviewQuestionSearchCondition;
import kr.co.deepple.interview.application.dto.InterviewQuestionView;
import kr.co.deepple.interview.application.dto.QInterviewQuestionView;
import kr.co.deepple.interview.application.port.out.InterviewQuestionQueryRepository;
import kr.co.deepple.interview.domain.InterviewCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterviewQuestionQueryRepositoryImpl implements InterviewQuestionQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<InterviewQuestionView> findAllQuestionByCategoryWithMemberId(
      InterviewQuestionSearchCondition condition, long memberId) {
    return queryFactory
        .select(
            new QInterviewQuestionView(
                interviewQuestion.id,
                interviewQuestion.content,
                interviewQuestion.category.stringValue(),
                interviewAnswer.id.isNotNull(),
                interviewAnswer.id,
                interviewAnswer.content))
        .from(interviewQuestion)
        .leftJoin(interviewAnswer)
        .on(
            interviewAnswer
                .questionId
                .eq(interviewQuestion.id)
                .and(interviewAnswer.memberId.eq(memberId)))
        .where(categoryEq(condition.category()), isPublic())
        .fetch();
  }

  @Override
  public Optional<InterviewQuestionView> findQuestionByIdWithMemberId(
      long questionId, long memberId) {
    return Optional.ofNullable(
        queryFactory
            .select(
                new QInterviewQuestionView(
                    interviewQuestion.id,
                    interviewQuestion.content,
                    interviewQuestion.category.stringValue(),
                    interviewAnswer.id.isNotNull(),
                    interviewAnswer.id,
                    interviewAnswer.content))
            .from(interviewQuestion)
            .leftJoin(interviewAnswer)
            .on(
                interviewAnswer
                    .questionId
                    .eq(interviewQuestion.id)
                    .and(interviewAnswer.memberId.eq(memberId)))
            .where(idEq(questionId), isPublic())
            .fetchOne());
  }

  private BooleanExpression categoryEq(String category) {
    if (category == null) {
      return null;
    }
    return interviewQuestion.category.eq(InterviewCategory.from(category));
  }

  private BooleanExpression idEq(long id) {
    return interviewQuestion.id.eq(id);
  }

  private BooleanExpression isPublic() {
    return interviewQuestion.isPublic.isTrue();
  }
}
