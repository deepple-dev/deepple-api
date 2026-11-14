package kr.co.deepple.admin.adapter.out.persistence;

import static kr.co.deepple.admin.domain.QScreening.screening;
import static kr.co.deepple.interview.domain.QInterviewAnswer.interviewAnswer;
import static kr.co.deepple.interview.domain.QInterviewQuestion.interviewQuestion;
import static kr.co.deepple.member.domain.QMember.member;
import static kr.co.deepple.member.domain.QProfileImage.profileImage;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import kr.co.deepple.admin.application.dto.QScreeningDetailInterviewView;
import kr.co.deepple.admin.application.dto.QScreeningDetailProfileImageView;
import kr.co.deepple.admin.application.dto.QScreeningDetailProfileView;
import kr.co.deepple.admin.application.dto.ScreeningDetailInterviewView;
import kr.co.deepple.admin.application.dto.ScreeningDetailProfileImageView;
import kr.co.deepple.admin.application.dto.ScreeningDetailProfileView;
import kr.co.deepple.admin.application.dto.ScreeningDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScreeningDetailQueryRepository {

  private final JPAQueryFactory queryFactory;

  public ScreeningDetailView findById(long screeningId) {
    long version =
        Optional.ofNullable(
                queryFactory
                    .select(screening.version)
                    .from(screening)
                    .where(screening.id.eq(screeningId))
                    .fetchOne())
            .orElse(-1L);

    ScreeningDetailProfileView profile =
        queryFactory
            .select(
                new QScreeningDetailProfileView(
                    member.id,
                    screening.status.stringValue(),
                    screening.rejectionReason.stringValue(),
                    member.profile.nickname.value,
                    member.profile.yearOfBirth.value,
                    member.profile.gender.stringValue(),
                    member.createdAt.stringValue()))
            .from(screening)
            .join(member)
            .on(screening.memberId.eq(member.id))
            .where(screening.id.eq(screeningId))
            .fetchOne();

    List<ScreeningDetailProfileImageView> profileImages =
        queryFactory
            .select(
                new QScreeningDetailProfileImageView(
                    profileImage.imageUrl.value, profileImage.order, profileImage.isPrimary))
            .from(profileImage)
            .where(profileImage.memberId.eq(profile.memberId()))
            .fetch();

    List<ScreeningDetailInterviewView> interviews =
        queryFactory
            .select(
                new QScreeningDetailInterviewView(
                    interviewQuestion.content, interviewAnswer.content))
            .from(interviewAnswer)
            .join(interviewQuestion)
            .on(interviewAnswer.questionId.eq(interviewQuestion.id))
            .where(
                interviewQuestion.isPublic.eq(true),
                interviewAnswer.memberId.eq(profile.memberId()))
            .fetch();

    return new ScreeningDetailView(screeningId, version, profile, profileImages, interviews);
  }
}
