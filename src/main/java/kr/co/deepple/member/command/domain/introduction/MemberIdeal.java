package kr.co.deepple.member.command.domain.introduction;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.introduction.vo.AgeRange;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.DrinkingStatus;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.member.command.domain.member.Religion;
import kr.co.deepple.member.command.domain.member.SmokingStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "member_ideals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberIdeal extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  @Embedded private AgeRange ageRange;

  @ElementCollection
  @CollectionTable(
      name = "member_ideal_hobbies",
      joinColumns = @JoinColumn(name = "member_ideal_id"))
  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Set<Hobby> hobbies = new HashSet<>();

  @ElementCollection
  @CollectionTable(
      name = "member_ideal_cities",
      joinColumns = @JoinColumn(name = "member_ideal_id"))
  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Set<City> cities = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Religion religion;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private SmokingStatus smokingStatus;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private DrinkingStatus drinkingStatus;

  private MemberIdeal(
      long memberId,
      @NonNull AgeRange ageRange,
      @NonNull Set<Hobby> hobbies,
      Set<City> cities,
      Religion religion,
      SmokingStatus smokingStatus,
      DrinkingStatus drinkingStatus) {
    this.memberId = memberId;
    this.ageRange = ageRange;
    this.hobbies = hobbies;
    setCities(cities);
    this.religion = religion;
    this.smokingStatus = smokingStatus;
    this.drinkingStatus = drinkingStatus;
  }

  public static MemberIdeal init(long memberId) {
    return new MemberIdeal(
        memberId, AgeRange.init(), new HashSet<>(), new HashSet<>(), null, null, null);
  }

  public void update(
      @NonNull AgeRange ageRange,
      @NonNull Set<Hobby> hobbies,
      Set<City> cities,
      Religion religion,
      SmokingStatus smokingStatus,
      DrinkingStatus drinkingStatus) {
    this.ageRange = ageRange;
    this.hobbies = hobbies;
    setCities(cities);
    this.religion = religion;
    this.smokingStatus = smokingStatus;
    this.drinkingStatus = drinkingStatus;
  }

  public boolean isUpdated() {
    return getUpdatedAt().isAfter(getCreatedAt());
  }

  private void setCities(@NonNull Set<City> cities) {
    if (cities.size() > 2) {
      throw new DeeppleException(
          MemberErrorCode.INVALID_IDEAL_CITY_COUNT, "count=" + cities.size());
    }
    this.cities = cities;
  }
}
