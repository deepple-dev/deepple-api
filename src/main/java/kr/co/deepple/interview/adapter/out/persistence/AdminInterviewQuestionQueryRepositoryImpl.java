package kr.co.deepple.interview.adapter.out.persistence;

import static kr.co.deepple.interview.domain.QInterviewQuestion.interviewQuestion;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.interview.application.dto.AdminInterviewQuestionView;
import kr.co.deepple.interview.application.dto.QAdminInterviewQuestionView;
import kr.co.deepple.interview.application.port.out.AdminInterviewQuestionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminInterviewQuestionQueryRepositoryImpl
    implements AdminInterviewQuestionQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<AdminInterviewQuestionView> findAdminInterviewQuestionPage(Pageable pageable) {
    List<AdminInterviewQuestionView> content =
        queryFactory
            .select(
                new QAdminInterviewQuestionView(
                    interviewQuestion.id,
                    interviewQuestion.content,
                    interviewQuestion.category.stringValue(),
                    interviewQuestion.isPublic,
                    interviewQuestion.createdAt))
            .from(interviewQuestion)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(interviewQuestion.id.desc())
            .fetch();

    long totalCount =
        Optional.ofNullable(
                queryFactory.select(interviewQuestion.count()).from(interviewQuestion).fetchOne())
            .orElse(0L);

    return new PageImpl<>(content, pageable, totalCount);
  }

  @Override
  public Optional<AdminInterviewQuestionView> findAdminInterviewQuestionById(long id) {
    return Optional.ofNullable(
        queryFactory
            .select(
                new QAdminInterviewQuestionView(
                    interviewQuestion.id,
                    interviewQuestion.content,
                    interviewQuestion.category.stringValue(),
                    interviewQuestion.isPublic,
                    interviewQuestion.createdAt))
            .from(interviewQuestion)
            .where(interviewQuestion.id.eq(id))
            .fetchOne());
  }
}
