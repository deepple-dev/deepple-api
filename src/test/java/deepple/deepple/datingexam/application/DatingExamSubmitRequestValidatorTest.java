package deepple.deepple.datingexam.application;

import deepple.deepple.datingexam.application.dto.DatingExamAnswerInfo;
import deepple.deepple.datingexam.application.dto.DatingExamInfoResponse;
import deepple.deepple.datingexam.application.dto.DatingExamQuestionInfo;
import deepple.deepple.datingexam.application.dto.DatingExamSubjectInfo;
import deepple.deepple.datingexam.application.exception.InvalidDatingExamSubmitRequestException;
import deepple.deepple.datingexam.domain.SubjectType;
import deepple.deepple.datingexam.domain.dto.AnswerSubmitRequest;
import deepple.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatingExamSubmitRequestValidatorTest {

    private static DatingExamInfoResponse buildInfo(SubjectType subjectType) {
        DatingExamAnswerInfo a111 = new DatingExamAnswerInfo(111L, "ans111");
        DatingExamAnswerInfo a112 = new DatingExamAnswerInfo(112L, "ans112");
        DatingExamAnswerInfo a113 = new DatingExamAnswerInfo(113L, "ans113");
        DatingExamQuestionInfo q11 = new DatingExamQuestionInfo(11L, "q11", List.of(a111, a112));
        DatingExamQuestionInfo q12 = new DatingExamQuestionInfo(12L, "q12", List.of(a113));
        DatingExamSubjectInfo subj1 = new DatingExamSubjectInfo(
            1L, subjectType.name(), "Subj1", List.of(q11, q12)
        );

        DatingExamAnswerInfo a211 = new DatingExamAnswerInfo(211L, "ans211");
        DatingExamQuestionInfo q21 = new DatingExamQuestionInfo(21L, "q21", List.of(a211));
        DatingExamSubjectInfo subj2 = new DatingExamSubjectInfo(
            2L, subjectType.name(), "Subj2", List.of(q21)
        );

        return new DatingExamInfoResponse(List.of(subj1, subj2));
    }

    @Test
    @DisplayName("유효한 과목을 제출하면, 예외를 던지지 않는다.")
    void validSubject() {
        var info = buildInfo(SubjectType.OPTIONAL);
        var request = new DatingExamSubmitRequest(1L, List.of(
            new AnswerSubmitRequest(11L, 111L),
            new AnswerSubmitRequest(12L, 113L)

        ));

        assertThatCode(() ->
            DatingExamSubmitRequestValidator.validateSubmit(request, info)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 subjectId를 제출하면, 예외를 던진다.")
    void missingSubjectIdShouldFail() {
        var info = buildInfo(SubjectType.OPTIONAL);
        var request = new DatingExamSubmitRequest(3L, List.of(
            new AnswerSubmitRequest(11L, 111L)
        ));

        assertThatThrownBy(() ->
            DatingExamSubmitRequestValidator.validateSubmit(request, info)
        )
            .isInstanceOf(InvalidDatingExamSubmitRequestException.class)
            .hasMessageContaining("존재하지 않는 subjectId");
    }

    @Test
    @DisplayName("질문ID가 중복되면, 예외를 던진다.")
    void duplicateQuestionIdsShouldFail() {
        var info = buildInfo(SubjectType.OPTIONAL);
        var request = new DatingExamSubmitRequest(1L, List.of(
            new AnswerSubmitRequest(11L, 111L),
            new AnswerSubmitRequest(11L, 112L)
        ));

        assertThatThrownBy(() ->
            DatingExamSubmitRequestValidator.validateSubmit(request, info)
        )
            .isInstanceOf(InvalidDatingExamSubmitRequestException.class)
            .hasMessageContaining("중복된 questionId");
    }

    @Test
    @DisplayName("답변 개수가 올바르지 않으면, 예외를 던진다.")
    void wrongAnswerCountShouldFail() {
        var info = buildInfo(SubjectType.OPTIONAL);
        var request = new DatingExamSubmitRequest(1L, List.of(
            new AnswerSubmitRequest(11L, 111L)
        ));

        assertThatThrownBy(() ->
            DatingExamSubmitRequestValidator.validateSubmit(request, info)
        )
            .isInstanceOf(InvalidDatingExamSubmitRequestException.class)
            .hasMessageContaining("속하는 questionId의 수가 올바르지 않습니다");
    }

    @Test
    @DisplayName("존재하지 않는 questionId를 제출하면, 예외를 던진다.")
    void missingQuestionIdShouldFail() {
        var info = buildInfo(SubjectType.OPTIONAL);
        var request = new DatingExamSubmitRequest(1L, List.of(
            new AnswerSubmitRequest(99L, 111L),
            new AnswerSubmitRequest(12L, 113L)
        ));

        assertThatThrownBy(() ->
            DatingExamSubmitRequestValidator.validateSubmit(request, info)
        )
            .isInstanceOf(InvalidDatingExamSubmitRequestException.class)
            .hasMessageContaining("속하지 않은 questionId");
    }

    @Test
    @DisplayName("존재하지 않는 answerId를 제출하면, 예외를 던진다.")
    void missingAnswerIdShouldFail() {
        var info = buildInfo(SubjectType.OPTIONAL);
        var request = new DatingExamSubmitRequest(1L, List.of(
            new AnswerSubmitRequest(11L, 999L),
            new AnswerSubmitRequest(12L, 113L)
        ));

        assertThatThrownBy(() ->
            DatingExamSubmitRequestValidator.validateSubmit(request, info)
        )
            .isInstanceOf(InvalidDatingExamSubmitRequestException.class)
            .hasMessageContaining("속하지 않은 answerId");
    }
}
