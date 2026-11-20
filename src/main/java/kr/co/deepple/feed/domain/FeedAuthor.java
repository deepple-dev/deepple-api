package kr.co.deepple.feed.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "feed_authors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedAuthor extends BaseEntity {

  private static final String ACTIVE = "ACTIVE";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)", nullable = false)
  private FeedAuthorType authorType;

  private Long memberId;

  private String nickname;

  @Column(columnDefinition = "varchar(50)")
  private String gender;

  private Integer yearOfBirth;

  @Column(columnDefinition = "varchar(50)")
  private String city;

  @Column(columnDefinition = "varchar(50)")
  private String mbti;

  private String profileImageUrl;

  @Column(columnDefinition = "varchar(50)")
  private String personalityType;

  @Column(columnDefinition = "varchar(50)")
  private String activityStatus;

  @Column(columnDefinition = "varchar(50)")
  private String grade;

  private Instant profileUpdatedAt;

  private Instant profileImageUpdatedAt;

  private Instant personalityTypeUpdatedAt;

  private Instant activityStatusUpdatedAt;

  private Instant gradeUpdatedAt;

  private FeedAuthor(FeedAuthorType authorType, Long memberId) {
    this.authorType = authorType;
    this.memberId = memberId;
  }

  public static FeedAuthor member(long memberId) {
    return new FeedAuthor(FeedAuthorType.MEMBER, memberId);
  }

  public void updateProfile(
      String nickname,
      String gender,
      Integer yearOfBirth,
      String city,
      String mbti,
      @NonNull Instant occurredAt) {
    if (isStale(profileUpdatedAt, occurredAt)) {
      return;
    }
    this.nickname = nickname;
    this.gender = gender;
    this.yearOfBirth = yearOfBirth;
    this.city = city;
    this.mbti = mbti;
    this.profileUpdatedAt = occurredAt;
  }

  public void updateProfileImage(String profileImageUrl, @NonNull Instant occurredAt) {
    if (isStale(profileImageUpdatedAt, occurredAt)) {
      return;
    }
    this.profileImageUrl = profileImageUrl;
    this.profileImageUpdatedAt = occurredAt;
  }

  public void updatePersonalityType(@NonNull String personalityType, @NonNull Instant occurredAt) {
    if (isStale(personalityTypeUpdatedAt, occurredAt)) {
      return;
    }
    this.personalityType = personalityType;
    this.personalityTypeUpdatedAt = occurredAt;
  }

  public void updateActivityStatus(@NonNull String activityStatus, @NonNull Instant occurredAt) {
    if (isStale(activityStatusUpdatedAt, occurredAt)) {
      return;
    }
    this.activityStatus = activityStatus;
    this.activityStatusUpdatedAt = occurredAt;
  }

  public void updateGrade(@NonNull String grade, @NonNull Instant occurredAt) {
    if (isStale(gradeUpdatedAt, occurredAt)) {
      return;
    }
    this.grade = grade;
    this.gradeUpdatedAt = occurredAt;
  }

  public boolean isOfficial() {
    return authorType == FeedAuthorType.OFFICIAL;
  }

  public boolean isActive() {
    return ACTIVE.equals(activityStatus);
  }

  private boolean isStale(Instant lastUpdatedAt, Instant occurredAt) {
    return lastUpdatedAt != null && !occurredAt.isAfter(lastUpdatedAt);
  }
}
