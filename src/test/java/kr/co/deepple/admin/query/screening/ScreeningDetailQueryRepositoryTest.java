package kr.co.deepple.admin.query.screening;

import kr.co.deepple.admin.command.domain.screening.Screening;
import kr.co.deepple.admin.fixture.ScreeningFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.interview.command.domain.answer.InterviewAnswer;
import kr.co.deepple.interview.command.domain.question.InterviewCategory;
import kr.co.deepple.interview.command.domain.question.InterviewQuestion;
import kr.co.deepple.interview.fixture.InterviewAnswerFixture;
import kr.co.deepple.interview.fixture.InterviewQuestionFixture;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.profileImage.ProfileImage;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

class ScreeningDetailQueryRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ScreeningDetailQueryRepository screeningDetailQueryRepository;

    @Test
    @DisplayName("상세 심사를 조회합니다.")
    void findById() {
        // given
        Member member = createMember();
        long memberId = (Long) em.persistAndGetId(member);

        Screening screening = ScreeningFixture.builder().memberId(memberId).build();
        long screeningId = (Long) em.persistAndGetId(screening);

        ProfileImage profileImage1 = createProfileImage(memberId, "image_url_1", true);
        ProfileImage profileImage2 = createProfileImage(memberId, "image_url_2", false);
        em.persist(profileImage1);
        em.persist(profileImage2);

        InterviewQuestion question1 = createInterviewQuestion("question1", true);
        InterviewQuestion question2 = createInterviewQuestion("question2", false);
        InterviewQuestion question3 = createInterviewQuestion("question3", true);
        long question1Id = (Long) em.persistAndGetId(question1);
        long question2Id = (Long) em.persistAndGetId(question2);
        em.persist(question3);

        InterviewAnswer answer1 = InterviewAnswerFixture.builder()
            .questionId(question1Id)
            .memberId(memberId)
            .content("answer for question1")
            .build();
        InterviewAnswer answer2 = InterviewAnswerFixture.builder()
            .questionId(question2Id)
            .memberId(memberId)
            .content("answer for question2")
            .build();
        em.persist(answer1);
        em.persist(answer2);

        em.flush();
        em.clear();

        // when
        ScreeningDetailView screeningDetail = screeningDetailQueryRepository.findById(screeningId);

        // then
        assertThat(screeningDetail).isNotNull();

        assertThat(screeningDetail.screeningId()).isEqualTo(screening.getId());
        assertThat(screeningDetail.profile().nickname()).isEqualTo("member");
        assertThat(screeningDetail.profile().gender()).isEqualTo("MALE");
        assertThat(screeningDetail.profile().screeningStatus()).isEqualTo(screening.getStatus().toString());
        assertThat(screeningDetail.profile().rejectionReason()).isNull();

        assertThat(screeningDetail.profileImages()).hasSize(2);
        assertThat(screeningDetail.profileImages()).extracting("imageUrl")
            .containsExactlyInAnyOrder("image_url_1", "image_url_2");
        assertThat(screeningDetail.profileImages()).extracting("isPrimary")
            .containsExactlyInAnyOrder(true, false);

        assertThat(screeningDetail.interviews()).hasSize(1);
        assertThat(screeningDetail.interviews().getFirst().question()).isEqualTo("question1");
        assertThat(screeningDetail.interviews().getFirst().answer()).isEqualTo("answer for question1");
    }

    private Member createMember() {
        return MemberFixture.builder()
            .phoneNumber("01011111111")
            .nickname("member")
            .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25)
            .gender(Gender.MALE)
            .build();
    }

    private ProfileImage createProfileImage(long memberId, String imageUrl, boolean isPrimary) {
        return ProfileImageFixture.builder()
            .memberId(memberId)
            .imageUrl(imageUrl)
            .primary(isPrimary)
            .build();
    }

    private InterviewQuestion createInterviewQuestion(String question1, boolean isPublic) {
        return InterviewQuestionFixture.builder()
            .content(question1)
            .category(InterviewCategory.PERSONAL)
            .publicQuestion(isPublic)
            .build();
    }
}
