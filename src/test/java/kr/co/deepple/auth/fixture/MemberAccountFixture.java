package kr.co.deepple.auth.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.MemberAccountStatus;
import kr.co.deepple.auth.domain.PhoneNumber;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberAccountFixture {

  private static final AtomicLong PHONE_NUMBER_SEQUENCE = new AtomicLong(50_000_000L);

  private Long id;
  private String phoneNumber;
  private MemberAccountStatus status = MemberAccountStatus.ACTIVE;
  private Instant suspendedUntil;
  private boolean deleted;
  private LocalDateTime deletedAt;

  public static MemberAccountFixture builder() {
    return new MemberAccountFixture();
  }

  private static String nextPhoneNumber() {
    return "010" + String.format("%08d", PHONE_NUMBER_SEQUENCE.incrementAndGet());
  }

  public MemberAccount build() {
    MemberAccount account =
        MemberAccount.builder()
            .id(id)
            .phoneNumber(PhoneNumber.from(phoneNumber == null ? nextPhoneNumber() : phoneNumber))
            .status(status)
            .suspendedUntil(suspendedUntil)
            .build();
    if (deleted || deletedAt != null) {
      setField(account, "deletedAt", deletedAt == null ? LocalDateTime.now() : deletedAt);
    }
    return account;
  }
}
