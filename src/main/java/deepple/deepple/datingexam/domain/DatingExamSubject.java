package deepple.deepple.datingexam.domain;

import deepple.deepple.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
public class DatingExamSubject extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private SubjectType type;

    private boolean isPublic;

    private DatingExamSubject(String name, SubjectType type) {
        setName(name);
        setType(type);
        this.isPublic = true;
    }

    public static DatingExamSubject create(String name, SubjectType type) {
        return new DatingExamSubject(name, type);
    }

    private void setName(@NonNull String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Subject name cannot be null or blank");
        }
        this.name = name;
    }

    private void setType(@NonNull SubjectType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return this.type == SubjectType.REQUIRED;
    }
}
