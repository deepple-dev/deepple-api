package deepple.deepple.member.fixture;

import deepple.deepple.member.command.domain.introduction.MemberIdeal;
import deepple.deepple.member.command.domain.introduction.vo.AgeRange;
import deepple.deepple.member.command.domain.member.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberIdealFixture {

    private Long id;
    private long memberId = 1L;
    private Integer minAge = 20;
    private Integer maxAge = 46;
    private Set<Hobby> hobbies = new HashSet<>();
    private Set<City> cities = new HashSet<>();
    private Religion religion;
    private SmokingStatus smokingStatus;
    private DrinkingStatus drinkingStatus;

    public static MemberIdealFixture builder() {
        return new MemberIdealFixture();
    }

    public MemberIdeal build() {
        MemberIdeal ideal = MemberIdeal.init(memberId);
        setField(ideal, "id", id);
        setField(ideal, "ageRange", AgeRange.of(minAge, maxAge));
        setField(ideal, "hobbies", new HashSet<>(hobbies));
        setField(ideal, "cities", new HashSet<>(cities));
        setField(ideal, "religion", religion);
        setField(ideal, "smokingStatus", smokingStatus);
        setField(ideal, "drinkingStatus", drinkingStatus);
        return ideal;
    }
}
