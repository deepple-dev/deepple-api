package deepple.deepple.member.command.domain.member.vo;

import deepple.deepple.member.command.domain.member.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberProfile {

    @Embedded
    private Nickname nickname;

    @Embedded
    private YearOfBirth yearOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private Gender gender;

    private Integer height;

    @ElementCollection
    @CollectionTable(name = "member_hobbies", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "name", columnDefinition = "varchar(50)")
    @Enumerated(EnumType.STRING)
    private Set<Hobby> hobbies = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private Mbti mbti;

    @Embedded
    @Setter
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private Religion religion;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private SmokingStatus smokingStatus;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private DrinkingStatus drinkingStatus;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private HighestEducation highestEducation;

    @Builder
    private MemberProfile(
        Nickname nickname, Integer yearOfBirth, Gender gender, Integer height,
        Mbti mbti, Region region, Religion religion,
        SmokingStatus smokingStatus, DrinkingStatus drinkingStatus, HighestEducation highestEducation,
        Job job, Set<Hobby> hobbies
    ) {
        this.nickname = nickname;
        this.yearOfBirth = YearOfBirth.from(yearOfBirth);
        this.gender = gender;
        this.height = height;
        this.mbti = mbti;
        this.region = region;
        this.religion = religion;
        this.smokingStatus = smokingStatus;
        this.drinkingStatus = drinkingStatus;
        this.highestEducation = highestEducation;
        this.job = job;
        this.hobbies = hobbies;
    }

    public boolean isProfileSettingNeeded() {
        return nickname == null || yearOfBirth == null || yearOfBirth.getValue() == null || gender == null
            || height == null || job == null ||
            hobbies == null || hobbies.isEmpty() || mbti == null || region == null || religion == null ||
            smokingStatus == null || drinkingStatus == null || highestEducation == null;
    }
}
