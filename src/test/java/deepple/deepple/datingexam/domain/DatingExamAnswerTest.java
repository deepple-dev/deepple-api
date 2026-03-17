package deepple.deepple.datingexam.domain;

import deepple.deepple.datingexam.domain.exception.InvalidDatingExamAnswerContentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatingExamAnswerTest {

    @Test
    @DisplayName("content이 null 또는 빈 문자열인 경우 예외가 발생한다.")
    void createWithNullOrEmptyContent() {
        // Given
        Long questionId = 1L;
        String nullContent = null;
        String emptyContent = "";
        AnswerPersonalityType personalityType = AnswerPersonalityType.DECISIVE_INDEPENDENT;

        // When & Then
        assertThatThrownBy(() -> DatingExamAnswer.create(questionId, nullContent, personalityType))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> DatingExamAnswer.create(questionId, emptyContent, personalityType))
            .isInstanceOf(InvalidDatingExamAnswerContentException.class);
    }

    @Test
    @DisplayName("questionId이 null인 경우 예외가 발생한다.")
    void createWithNullQuestionId() {
        // Given
        String content = "Valid Answer Content";
        Long nullQuestionId = null;
        AnswerPersonalityType personalityType = AnswerPersonalityType.DECISIVE_INDEPENDENT;

        // When & Then
        assertThatThrownBy(() -> DatingExamAnswer.create(nullQuestionId, content, personalityType))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("personalityType이 null인 경우 예외가 발생한다.")
    void createWithNullPersonalityType() {
        // Given
        Long questionId = 1L;
        String content = "Valid Answer Content";
        AnswerPersonalityType nullPersonalityType = null;

        // When & Then
        assertThatThrownBy(() -> DatingExamAnswer.create(questionId, content, nullPersonalityType))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("정상적인 answer 생성")
    void createWithValidParameters() {
        // Given
        Long questionId = 1L;
        String content = "Valid Answer Content";
        AnswerPersonalityType personalityType = AnswerPersonalityType.DEVOTED_ROMANTIC;

        // When
        DatingExamAnswer answer = DatingExamAnswer.create(questionId, content, personalityType);

        // Then
        assertThat(answer.getQuestionId()).isEqualTo(questionId);
        assertThat(answer.getContent()).isEqualTo(content);
        assertThat(answer.getPersonalityType()).isEqualTo(personalityType);
    }
}
