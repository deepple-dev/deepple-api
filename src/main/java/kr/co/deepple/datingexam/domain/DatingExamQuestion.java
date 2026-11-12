package kr.co.deepple.datingexam.domain;

import kr.co.deepple.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class DatingExamQuestion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long subjectId;

    @Column(nullable = false)
    private String content;

    private DatingExamQuestion(Long subjectId, String content) {
        setSubjectId(subjectId);
        setContent(content);
    }

    public static DatingExamQuestion create(Long subjectId, String content) {
        return new DatingExamQuestion(subjectId, content);
    }

    private void setSubjectId(@NonNull Long subjectId) {
        this.subjectId = subjectId;
    }

    private void setContent(@NonNull String content) {
        if (content.isBlank()) {
            throw new IllegalArgumentException("Question content cannot be null or blank");
        }
        this.content = content;
    }
}
