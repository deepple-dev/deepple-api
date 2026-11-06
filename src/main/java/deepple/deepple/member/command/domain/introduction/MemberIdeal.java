package deepple.deepple.member.command.domain.introduction;

import deepple.deepple.common.entity.BaseEntity;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.introduction.vo.AgeRange;
import deepple.deepple.member.command.domain.member.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "member_ideals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberIdeal extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(unique = true)
    private Long memberId;

    @Getter
    @Embedded
    private AgeRange ageRange;

    @Getter
    @ElementCollection
    @CollectionTable(name = "member_ideal_hobbies", joinColumns = @JoinColumn(name = "member_ideal_id"))
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private Set<Hobby> hobbies = new HashSet<>();

    @Getter
    @ElementCollection
    @CollectionTable(name = "member_ideal_cities", joinColumns = @JoinColumn(name = "member_ideal_id"))
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private Set<City> cities = new HashSet<>();

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private Religion religion;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private SmokingStatus smokingStatus;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private DrinkingStatus drinkingStatus;

    private MemberIdeal(
        Long memberId,
        AgeRange ageRange,
        Set<Hobby> hobbies,
        Set<City> cities,
        Religion religion,
        SmokingStatus smokingStatus,
        DrinkingStatus drinkingStatus
    ) {
        setMemberId(memberId);
        setAgeRange(ageRange);
        setHobbies(hobbies);
        setCities(cities);
        this.religion = religion;
        this.smokingStatus = smokingStatus;
        this.drinkingStatus = drinkingStatus;
    }

    public static MemberIdeal init(Long memberId) {
        return new MemberIdeal(memberId, AgeRange.init(), new HashSet<>(), new HashSet<>(), null, null, null);
    }

    public void update(
        AgeRange ageRange,
        Set<Hobby> hobbies,
        Set<City> cities,
        Religion religion,
        SmokingStatus smokingStatus,
        DrinkingStatus drinkingStatus
    ) {
        setAgeRange(ageRange);
        setHobbies(hobbies);
        setCities(cities);
        this.religion = religion;
        this.smokingStatus = smokingStatus;
        this.drinkingStatus = drinkingStatus;
    }

    public boolean isUpdated() {
        return getUpdatedAt().isAfter(getCreatedAt());
    }

    private void setMemberId(@NonNull Long memberId) {
        this.memberId = memberId;
    }

    private void setAgeRange(@NonNull AgeRange ageRange) {
        this.ageRange = ageRange;
    }

    private void setHobbies(@NonNull Set<Hobby> hobbies) {
        this.hobbies = hobbies;
    }

    private void setCities(@NonNull Set<City> cities) {
        if (cities.size() > 2) {
            throw new DeeppleException(MemberErrorCode.INVALID_IDEAL_CITY_COUNT, "count=" + cities.size());
        }
        this.cities = cities;
    }
}
