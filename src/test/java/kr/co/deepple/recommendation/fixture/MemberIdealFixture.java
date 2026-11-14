package kr.co.deepple.recommendation.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.recommendation.domain.AgeRange;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberIdealFixture {

  private Long id;
  private long memberId = 1L;
  private Integer minAge = 20;
  private Integer maxAge = 46;
  private Set<String> hobbies = new HashSet<>();
  private Set<String> cities = new HashSet<>();
  private String religion;
  private String smokingStatus;
  private String drinkingStatus;

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
