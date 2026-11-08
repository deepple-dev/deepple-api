package deepple.deepple.member.fixture;

import deepple.deepple.member.command.domain.introduction.IntroductionType;
import deepple.deepple.member.command.domain.introduction.MemberIntroduction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberIntroductionFixture {

    private Long id;
    private long memberId = 1L;
    private long introducedMemberId = 2L;
    private IntroductionType type = IntroductionType.DIAMOND_GRADE;

    public static MemberIntroductionFixture builder() {
        return new MemberIntroductionFixture();
    }

    public MemberIntroduction build() {
        MemberIntroduction introduction = MemberIntroduction.ofWithoutCharge(memberId, introducedMemberId, type);
        setField(introduction, "id", id);
        return introduction;
    }
}
