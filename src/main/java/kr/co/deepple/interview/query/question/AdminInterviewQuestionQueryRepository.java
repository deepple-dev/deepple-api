package kr.co.deepple.interview.query.question;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.deepple.interview.query.question.view.AdminInterviewQuestionView;
import kr.co.deepple.interview.query.question.view.QAdminInterviewQuestionView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static kr.co.deepple.interview.command.domain.question.QInterviewQuestion.interviewQuestion;

@Repository
@RequiredArgsConstructor
public class AdminInterviewQuestionQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<AdminInterviewQuestionView> findAdminInterviewQuestionPage(Pageable pageable) {
        List<AdminInterviewQuestionView> content = queryFactory
            .select(new QAdminInterviewQuestionView(
                interviewQuestion.id,
                interviewQuestion.content,
                interviewQuestion.category.stringValue(),
                interviewQuestion.isPublic,
                interviewQuestion.createdAt
            ))
            .from(interviewQuestion)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(interviewQuestion.id.desc())
            .fetch();

        long totalCount = Optional.ofNullable(queryFactory
            .select(interviewQuestion.count())
            .from(interviewQuestion)
            .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, totalCount);
    }

    public Optional<AdminInterviewQuestionView> findAdminInterviewQuestionById(Long id) {
        return Optional.ofNullable(queryFactory
            .select(new QAdminInterviewQuestionView(
                interviewQuestion.id,
                interviewQuestion.content,
                interviewQuestion.category.stringValue(),
                interviewQuestion.isPublic,
                interviewQuestion.createdAt
            ))
            .from(interviewQuestion)
            .where(interviewQuestion.id.eq(id))
            .fetchOne()
        );
    }
}
