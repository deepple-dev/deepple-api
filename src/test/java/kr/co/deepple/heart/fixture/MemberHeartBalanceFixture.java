package kr.co.deepple.heart.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.heart.domain.HeartBalance;
import kr.co.deepple.heart.domain.MemberHeartBalance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberHeartBalanceFixture {

  private Long id;
  private long memberId = 1L;
  private long purchaseBalance;
  private long missionBalance;

  public static MemberHeartBalanceFixture builder() {
    return new MemberHeartBalanceFixture();
  }

  public MemberHeartBalance build() {
    MemberHeartBalance balance =
        MemberHeartBalance.of(memberId, HeartBalance.of(missionBalance, purchaseBalance));
    setField(balance, "id", id);
    return balance;
  }
}
