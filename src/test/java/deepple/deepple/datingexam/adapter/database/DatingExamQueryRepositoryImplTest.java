package deepple.deepple.datingexam.adapter.database;

import deepple.deepple.common.config.QueryDslConfig;
import deepple.deepple.datingexam.application.dto.DatingExamAnswerInfo;
import deepple.deepple.datingexam.application.dto.DatingExamInfoResponse;
import deepple.deepple.datingexam.application.dto.DatingExamQuestionInfo;
import deepple.deepple.datingexam.application.dto.DatingExamSubjectInfo;
import deepple.deepple.datingexam.application.required.DatingExamQueryRepository;
import deepple.deepple.datingexam.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import({QueryDslConfig.class, DatingExamQueryRepositoryImpl.class})
@DataJpaTest
class DatingExamQueryRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private DatingExamQueryRepository repo;

    private DatingExamSubject subj1;
    private DatingExamQuestion q11, q12;
    private DatingExamAnswer a111, a112, a113;

    @BeforeEach
    void setUp() {
        subj1 = DatingExamSubject.create("Subj1", SubjectType.OPTIONAL);
        em.persist(subj1);

        q11 = DatingExamQuestion.create(subj1.getId(), "q11");
        q12 = DatingExamQuestion.create(subj1.getId(), "q12");
        em.persist(q11);
        em.persist(q12);

        a111 = DatingExamAnswer.create(q11.getId(), "ans111", AnswerPersonalityType.DECISIVE_INDEPENDENT);
        a112 = DatingExamAnswer.create(q11.getId(), "ans112", AnswerPersonalityType.GROWING_RUNNING_MATE);
        a113 = DatingExamAnswer.create(q12.getId(), "ans113", AnswerPersonalityType.DEVOTED_ROMANTIC);
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
        DatingExamSubject subj2 = DatingExamSubject.create("Subj2", SubjectType.OPTIONAL);
        em.persist(subj2);

        DatingExamQuestion q21 = DatingExamQuestion.create(subj2.getId(), "q21");
        em.persist(q21);
        DatingExamAnswer a211 = DatingExamAnswer.create(q21.getId(), "ans211", AnswerPersonalityType.REALISTIC_SHELTER);
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
