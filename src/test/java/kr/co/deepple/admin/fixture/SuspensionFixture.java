package kr.co.deepple.admin.fixture;

import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.Instant;
import kr.co.deepple.admin.command.domain.suspension.Suspension;
import kr.co.deepple.admin.command.domain.suspension.SuspensionPolicy;
import kr.co.deepple.admin.command.domain.suspension.SuspensionStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SuspensionFixture {

  private Long id;
  private long adminId = 1L;
  private long memberId = 1L;
  private SuspensionStatus status = SuspensionStatus.TEMPORARY;
  private Instant expireAt = Instant.now().plus(SuspensionPolicy.TEMPORARY_SUSPENSION_DURATION);

  public static SuspensionFixture builder() {
    return new SuspensionFixture();
  }

  public Suspension build() {
    Suspension suspension = instantiateClass(Suspension.class);
    setField(suspension, "id", id);
    setField(suspension, "adminId", adminId);
    setField(suspension, "memberId", memberId);
    setField(suspension, "status", status);
    setField(suspension, "expireAt", status == SuspensionStatus.PERMANENT ? null : expireAt);
    return suspension;
  }
}
