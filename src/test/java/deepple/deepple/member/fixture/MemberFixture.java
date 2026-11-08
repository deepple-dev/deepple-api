package deepple.deepple.member.fixture;

import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import deepple.deepple.member.command.domain.member.*;
import deepple.deepple.member.command.domain.member.vo.KakaoId;
import deepple.deepple.member.command.domain.member.vo.PhoneNumber;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberFixture {

    private static final AtomicLong PHONE_NUMBER_SEQUENCE = new AtomicLong();

    private Long id;
    private String phoneNumber;
    private String kakaoId;
    private ActivityStatus activityStatus = ActivityStatus.ACTIVE;
    private Grade grade = Grade.SILVER;
    private PrimaryContactType primaryContactType = PrimaryContactType.NONE;
    private boolean vip;
    private boolean profilePublic;
    private long purchaseHeartBalance;
    private long missionHeartBalance;
    private boolean datingExamSubmitted;
    private LocalDateTime lastAccessedAt;

    @Setter(AccessLevel.NONE)
    private final MemberProfileFixture profile = MemberProfileFixture.builder();

    @Setter(AccessLevel.NONE)
    private boolean withoutProfile;

    public static MemberFixture builder() {
        return new MemberFixture();
    }

    private static String nextPhoneNumber() {
        return "010" + String.format("%08d", PHONE_NUMBER_SEQUENCE.incrementAndGet());
    }

    public MemberFixture noProfile() {
        withoutProfile = true;
        return this;
    }

    public MemberFixture nickname(String nickname) {
        profile.nickname(nickname);
        return this;
    }

    public MemberFixture yearOfBirth(Integer yearOfBirth) {
        profile.yearOfBirth(yearOfBirth);
        return this;
    }

    public MemberFixture gender(Gender gender) {
        profile.gender(gender);
        return this;
    }

    public MemberFixture height(Integer height) {
        profile.height(height);
        return this;
    }

    public MemberFixture mbti(Mbti mbti) {
        profile.mbti(mbti);
        return this;
    }

    public MemberFixture district(District district) {
        profile.district(district);
        return this;
    }

    public MemberFixture religion(Religion religion) {
        profile.religion(religion);
        return this;
    }

    public MemberFixture smokingStatus(SmokingStatus smokingStatus) {
        profile.smokingStatus(smokingStatus);
        return this;
    }

    public MemberFixture drinkingStatus(DrinkingStatus drinkingStatus) {
        profile.drinkingStatus(drinkingStatus);
        return this;
    }

    public MemberFixture highestEducation(HighestEducation highestEducation) {
        profile.highestEducation(highestEducation);
        return this;
    }

    public MemberFixture job(Job job) {
        profile.job(job);
        return this;
    }

    public MemberFixture hobbies(Set<Hobby> hobbies) {
        profile.hobbies(hobbies);
        return this;
    }

    public Member build() {
        return Member.builder()
            .id(id)
            .phoneNumber(PhoneNumber.from(phoneNumber == null ? nextPhoneNumber() : phoneNumber))
            .kakaoId(kakaoId == null ? null : KakaoId.from(kakaoId))
            .profile(withoutProfile ? null : profile.build())
            .activityStatus(activityStatus)
            .grade(grade)
            .primaryContactType(primaryContactType)
            .isVip(vip)
            .isProfilePublic(profilePublic)
            .heartBalance(HeartBalance.of(missionHeartBalance, purchaseHeartBalance))
            .isDatingExamSubmitted(datingExamSubmitted)
            .lastAccessedAt(lastAccessedAt)
            .build();
    }
}
