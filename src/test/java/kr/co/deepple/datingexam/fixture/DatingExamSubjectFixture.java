package kr.co.deepple.datingexam.fixture;

import kr.co.deepple.datingexam.domain.DatingExamSubject;
import kr.co.deepple.datingexam.domain.SubjectType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatingExamSubjectFixture {

    private Long id;
    private String name = "연애고사 과목";
    private SubjectType type = SubjectType.REQUIRED;
    private boolean publicSubject = true;

    public static DatingExamSubjectFixture builder() {
        return new DatingExamSubjectFixture();
    }

    public DatingExamSubject build() {
        DatingExamSubject subject = DatingExamSubject.create(name, type);
        setField(subject, "id", id);
        setField(subject, "isPublic", publicSubject);
        return subject;
    }
}
