package kr.co.deepple.admin.command.domain.suspension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SuspensionPolicyTest {

  SuspensionPolicy policy = new SuspensionPolicy();

  @Test
  @DisplayName("evaluate(): 경고 수가 0이면 null 반환")
  void evaluateZeroReturnsNull() {
    // given
    var warningCount = 0L;

    // when
    var result = policy.evaluate(warningCount);

    // then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("evaluate(): 경고 수가 임계값 미만(1 이상 3 미만)이면 TEMPORARY 반환")
  void evaluateBelowPermanentThresholdReturnsTemporary() {
    // given
    var warningCount = 2L;

    // when
    var result = policy.evaluate(warningCount);

    // then
    assertThat(result).isEqualTo(SuspensionStatus.TEMPORARY);
  }

  @Test
  @DisplayName("evaluate(): 경고 수가 PERMANENT 임계값과 같으면 PERMANENT 반환")
  void evaluateAtPermanentThresholdReturnsPermanent() {
    // given
    var warningCount = 3L;

    // when
    var result = policy.evaluate(warningCount);

    // then
    assertThat(result).isEqualTo(SuspensionStatus.PERMANENT);
  }

  @Test
  @DisplayName("evaluate(): 경고 수가 PERMANENT 임계값 초과이면 PERMANENT 반환")
  void evaluateAbovePermanentThresholdReturnsPermanent() {
    // given
    var warningCount = 5L;

    // when
    var result = policy.evaluate(warningCount);

    // then
    assertThat(result).isEqualTo(SuspensionStatus.PERMANENT);
  }
}
