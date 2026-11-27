package kr.co.deepple.auth.fixture;

import kr.co.deepple.auth.domain.MemberWithdrawal;
import kr.co.deepple.auth.domain.WithdrawalReason;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberWithdrawalFixture {

  private WithdrawalReason reason = WithdrawalReason.NO_REASON;

  public static MemberWithdrawalFixture builder() {
    return new MemberWithdrawalFixture();
  }

  public MemberWithdrawal build() {
    return MemberWithdrawal.from(reason);
  }
}
