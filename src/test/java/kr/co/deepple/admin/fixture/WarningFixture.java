package kr.co.deepple.admin.fixture;

import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.HashSet;
import java.util.Set;
import kr.co.deepple.admin.domain.Warning;
import kr.co.deepple.admin.domain.WarningReasonType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WarningFixture {

  private Long id;
  private long adminId = 1L;
  private long memberId = 1L;
  private Set<WarningReasonType> reasonTypes =
      new HashSet<>(Set.of(WarningReasonType.INAPPROPRIATE_PROFILE));
  private boolean critical;

  public static WarningFixture builder() {
    return new WarningFixture();
  }

  public Warning build() {
    Warning warning = instantiateClass(Warning.class);
    setField(warning, "id", id);
    setField(warning, "adminId", adminId);
    setField(warning, "memberId", memberId);
    setField(warning, "reasonTypes", new HashSet<>(reasonTypes));
    setField(warning, "isCritical", critical);
    return warning;
  }
}
