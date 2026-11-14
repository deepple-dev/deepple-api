package kr.co.deepple.admin.fixture;

import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.admin.domain.RejectionReasonType;
import kr.co.deepple.admin.domain.Screening;
import kr.co.deepple.admin.domain.ScreeningStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScreeningFixture {

  private Long id;
  private long memberId = 1L;
  private Long adminId;
  private RejectionReasonType rejectionReason;
  private ScreeningStatus status = ScreeningStatus.PENDING;
  private Long version;

  public static ScreeningFixture builder() {
    return new ScreeningFixture();
  }

  public Screening build() {
    Screening screening = instantiateClass(Screening.class);
    setField(screening, "id", id);
    setField(screening, "memberId", memberId);
    setField(screening, "adminId", adminId);
    setField(screening, "rejectionReason", rejectionReason);
    setField(screening, "status", status);
    setField(screening, "version", version);
    return screening;
  }
}
