package kr.co.deepple.member.fixture;

import kr.co.deepple.member.command.domain.member.*;
import kr.co.deepple.member.command.domain.member.vo.MemberProfile;
import kr.co.deepple.member.command.domain.member.vo.Nickname;
import kr.co.deepple.member.command.domain.member.vo.Region;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Year;
import java.util.HashSet;
import java.util.Set;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberProfileFixture {

    private String nickname = "딥플";
    private Integer yearOfBirth = Year.now().getValue() - 29;
    private Gender gender = Gender.MALE;
    private Integer height = 175;
    private Mbti mbti = Mbti.ENFP;
    private District district = District.GANGNAM_GU;
    private Religion religion = Religion.NONE;
    private SmokingStatus smokingStatus = SmokingStatus.NONE;
    private DrinkingStatus drinkingStatus = DrinkingStatus.NONE;
    private HighestEducation highestEducation = HighestEducation.BACHELORS_SEOUL;
    private Job job = Job.RESEARCH_AND_ENGINEERING;
    private Set<Hobby> hobbies = new HashSet<>(Set.of(Hobby.TRAVEL));

    public static MemberProfileFixture builder() {
        return new MemberProfileFixture();
    }

    public MemberProfile build() {
        return MemberProfile.builder()
            .nickname(nickname == null ? null : Nickname.from(nickname))
            .yearOfBirth(yearOfBirth)
            .gender(gender)
            .height(height)
            .mbti(mbti)
            .region(district == null ? null : Region.of(district))
            .religion(religion)
            .smokingStatus(smokingStatus)
            .drinkingStatus(drinkingStatus)
            .highestEducation(highestEducation)
            .job(job)
            .hobbies(hobbies == null ? null : new HashSet<>(hobbies))
            .build();
    }
}
