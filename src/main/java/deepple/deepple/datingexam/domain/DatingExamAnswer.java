package deepple.deepple.datingexam.domain;

import deepple.deepple.common.entity.BaseEntity;
import deepple.deepple.datingexam.domain.exception.InvalidDatingExamAnswerContentException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class DatingExamAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private AnswerPersonalityType personalityType;

    private DatingExamAnswer(Long questionId, String content, AnswerPersonalityType personalityType) {
        setQuestionId(questionId);
        setContent(content);
        setPersonalityType(personalityType);
    }

    public static DatingExamAnswer create(Long questionId, String content, AnswerPersonalityType personalityType) {
        return new DatingExamAnswer(questionId, content, personalityType);
    }

    private void setQuestionId(@NonNull Long questionId) {
        this.questionId = questionId;
    }

    private void setContent(@NonNull String content) {
        if (content.isBlank()) {
            throw new InvalidDatingExamAnswerContentException("Content cannot be null or blank");
        }
        this.content = content;
    }

    private void setPersonalityType(@NonNull AnswerPersonalityType personalityType) {
        this.personalityType = personalityType;
    }
}
