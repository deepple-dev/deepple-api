package kr.co.deepple.datingexam.adapter.out.persistence;

import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.datingexam.application.dto.DatingExamAnswerInfo;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoResponse;
import kr.co.deepple.datingexam.application.dto.DatingExamQuestionInfo;
import kr.co.deepple.datingexam.application.dto.DatingExamSubjectInfo;
import kr.co.deepple.datingexam.application.port.out.DatingExamQueryRepository;
import kr.co.deepple.datingexam.domain.*;
import kr.co.deepple.datingexam.fixture.DatingExamAnswerFixture;
import kr.co.deepple.datingexam.fixture.DatingExamQuestionFixture;
import kr.co.deepple.datingexam.fixture.DatingExamSubjectFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

class DatingExamQueryRepositoryImplTest extends RepositoryTestSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private DatingExamQueryRepository repo;

    private DatingExamSubject subj1;
    private DatingExamQuestion q11, q12;
    private DatingExamAnswer a111, a112, a113;

    @BeforeEach
    void setUp() {
        em.getEntityManager().createNativeQuery("delete from dating_exam_answer").executeUpdate();
        em.getEntityManager().createNativeQuery("delete from dating_exam_question").executeUpdate();
        em.getEntityManager().createNativeQuery("delete from dating_exam_subject").executeUpdate();

        subj1 = DatingExamSubjectFixture.builder()
            .name("Subj1")
            .type(SubjectType.OPTIONAL)
            .build();
        em.persist(subj1);

        q11 = DatingExamQuestionFixture.builder()
            .subjectId(subj1.getId())
            .content("q11")
            .build();
        q12 = DatingExamQuestionFixture.builder()
            .subjectId(subj1.getId())
            .content("q12")
            .build();
        em.persist(q11);
        em.persist(q12);

        a111 = DatingExamAnswerFixture.builder()
            .questionId(q11.getId())
            .content("ans111")
            .personalityType(AnswerPersonalityType.DECISIVE_INDEPENDENT)
            .build();
        a112 = DatingExamAnswerFixture.builder()
            .questionId(q11.getId())
            .content("ans112")
            .personalityType(AnswerPersonalityType.GROWING_RUNNING_MATE)
            .build();
        a113 = DatingExamAnswerFixture.builder()
            .questionId(q12.getId())
            .content("ans113")
            .personalityType(AnswerPersonalityType.DEVOTED_ROMANTIC)
            .build();
        em.persist(a111);
        em.persist(a112);
        em.persist(a113);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("OPTIONAL 타입은 subjectType이 OPTIONAL인 시험 정보를 조회하면, 올바른 DTO를 반환한다.")
    void findOptionalExamInfoReturnsCorrectDto() {
        // when
        DatingExamInfoResponse response = repo.findDatingExamInfo(SubjectType.OPTIONAL);

        // then: 과목이 하나만 조회된다
        assertThat(response.subjects()).hasSize(1);
        DatingExamSubjectInfo subjectInfo = response.subjects().get(0);
        assertThat(subjectInfo.id()).isEqualTo(subj1.getId());
        assertThat(subjectInfo.type()).isEqualTo(subj1.getType().name());
        assertThat(subjectInfo.name()).isEqualTo(subj1.getName());

        // then: 질문이 정확히 두 개 조회된다
        assertThat(subjectInfo.questions()).hasSize(2);

        // q11에 속한 답변만 [a111, a112]인지 검증
        DatingExamQuestionInfo questionInfo11 = subjectInfo.questions().stream()
            .filter(q -> q.id() == q11.getId())
            .findFirst()
            .orElseThrow();
        assertThat(questionInfo11.answers())
            .extracting(DatingExamAnswerInfo::id)
            .containsExactlyInAnyOrder(a111.getId(), a112.getId());

        // q12에 속한 답변만 [a113]인지 검증
        DatingExamQuestionInfo questionInfo12 = subjectInfo.questions().stream()
            .filter(q -> q.id() == q12.getId())
            .findFirst()
            .orElseThrow();
        assertThat(questionInfo12.answers())
            .extracting(DatingExamAnswerInfo::id)
            .containsExactly(a113.getId());
    }

    @Test
    @DisplayName("다른 subject의 question이 섞이지 않는다.")
    void optionalDoesNotIncludeQuestionsFromOtherSubjects() {
        // given: 새로운 OPTIONAL 과목과 그에 속한 질문/답변 추가
        DatingExamSubject subj2 = DatingExamSubjectFixture.builder()
            .name("Subj2")
            .type(SubjectType.OPTIONAL)
            .build();
        em.persist(subj2);

        DatingExamQuestion q21 = DatingExamQuestionFixture.builder()
            .subjectId(subj2.getId())
            .content("q21")
            .build();
        em.persist(q21);
        DatingExamAnswer a211 = DatingExamAnswerFixture.builder()
            .questionId(q21.getId())
            .content("ans211")
            .personalityType(AnswerPersonalityType.REALISTIC_SHELTER)
            .build();
        em.persist(a211);

        em.flush();
        em.clear();

        // when
        DatingExamInfoResponse response = repo.findDatingExamInfo(SubjectType.OPTIONAL);

        DatingExamSubjectInfo subjectInfo1 = response.subjects().stream()
            .filter(s -> s.id() == (subj1.getId()))
            .findFirst().orElseThrow();
        assertThat(subjectInfo1.questions())
            .extracting(DatingExamQuestionInfo::id)
            .containsExactlyInAnyOrder(q11.getId(), q12.getId())
            .doesNotContain(q21.getId());

        DatingExamSubjectInfo subjectInfo2 = response.subjects().stream()
            .filter(s -> s.id() == (subj2.getId()))
            .findFirst().orElseThrow();
        assertThat(subjectInfo2.questions())
            .extracting(DatingExamQuestionInfo::id)
            .containsExactly(q21.getId());

        DatingExamQuestionInfo questionInfo21 = subjectInfo2.questions().get(0);
        assertThat(questionInfo21.answers())
            .extracting(DatingExamAnswerInfo::id)
            .containsExactly(a211.getId());
    }

    @Test
    @DisplayName("subjectType에 맞는 데이터가 없는 경우, 조회 결과가 빈 리스트를 반환한다.")
    void findRequiredExamInfoReturnsEmpty() {
        // when
        DatingExamInfoResponse response = repo.findDatingExamInfo(SubjectType.REQUIRED);

        // then
        assertThat(response.subjects()).isEmpty();
    }
}
