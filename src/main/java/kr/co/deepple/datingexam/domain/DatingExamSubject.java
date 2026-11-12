package kr.co.deepple.datingexam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "dating_exam_subject")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatingExamSubject extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private SubjectType type;

  private boolean isPublic;

  private DatingExamSubject(@NonNull String name, @NonNull SubjectType type) {
    if (name.isBlank()) {
      throw new IllegalArgumentException("과목 이름은 비어 있을 수 없습니다.");
    }
    this.name = name;
    this.type = type;
    this.isPublic = true;
  }

  public static DatingExamSubject of(String name, SubjectType type) {
    return new DatingExamSubject(name, type);
  }

  public boolean isRequired() {
    return type == SubjectType.REQUIRED;
  }
}
