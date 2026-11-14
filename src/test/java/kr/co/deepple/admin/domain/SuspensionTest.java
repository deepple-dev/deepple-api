package kr.co.deepple.admin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.admin.fixture.SuspensionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SuspensionTest {

  @Test
  @DisplayName("createTemporary(): 유효한 입력 시 일시정지 Suspension 생성")
  void createTemporarySuspensionWithValidInputs() {
    // given
    long expectedAdminId = 42L;
    long expectedMemberId = 100L;
    var before = Instant.now();

    // when
    var suspension = Suspension.createTemporary(expectedAdminId, expectedMemberId);

    // then
    assertThat(suspension).isNotNull();
    assertThat(suspension.getId()).isNull();
    assertThat(suspension.getAdminId()).isEqualTo(expectedAdminId);
    assertThat(suspension.getMemberId()).isEqualTo(expectedMemberId);
    assertThat(suspension.getStatus()).isEqualTo(SuspensionStatus.TEMPORARY);
    assertThat(suspension.getExpireAt()).isNotNull();
    assertThat(suspension.getExpireAt()).isAfter(before);
  }

  @Test
  @DisplayName("createPermanent(): 유효한 입력 시 영구정지 Suspension 생성")
  void createPermanentSuspensionWithValidInputs() {
    // given
    long expectedAdminId = 1L;
    long expectedMemberId = 2L;

    // when
    var suspension = Suspension.createPermanent(expectedAdminId, expectedMemberId);

    // then
    assertThat(suspension).isNotNull();
    assertThat(suspension.getId()).isNull();
    assertThat(suspension.getAdminId()).isEqualTo(expectedAdminId);
    assertThat(suspension.getMemberId()).isEqualTo(expectedMemberId);
    assertThat(suspension.getStatus()).isEqualTo(SuspensionStatus.PERMANENT);
    assertThat(suspension.getExpireAt()).isNull();
  }

  @Test
  @DisplayName("changeToPermanent(): TEMPORARY 상태를 PERMANENT로 변경")
  void changeToPermanentFromTemporary() {
    // given
    var suspension =
        SuspensionFixture.builder()
            .adminId(1L)
            .memberId(2L)
            .status(SuspensionStatus.TEMPORARY)
            .build();
    var newAdminId = 123L;

    // when
    suspension.changeToPermanent(newAdminId);

    // then
    assertThat(suspension.getStatus()).isEqualTo(SuspensionStatus.PERMANENT);
    assertThat(suspension.getAdminId()).isEqualTo(newAdminId);
    assertThat(suspension.getExpireAt()).isNull();
  }

  @Test
  @DisplayName("changeToPermanent(): 이미 PERMANENT 상태인 경우 변경되지 않음")
  void changeToPermanentWhenAlreadyPermanent() {
    // given
    var suspension =
        SuspensionFixture.builder()
            .adminId(1L)
            .memberId(2L)
            .status(SuspensionStatus.PERMANENT)
            .expireAt(null)
            .build();
    var originalAdminId = suspension.getAdminId();
    var originalExpireAt = suspension.getExpireAt();

    // when
    suspension.changeToPermanent(999L);

    // then
    assertThat(suspension.getStatus()).isEqualTo(SuspensionStatus.PERMANENT);
    assertThat(suspension.getAdminId()).isEqualTo(originalAdminId);
    assertThat(suspension.getExpireAt()).isEqualTo(originalExpireAt);
  }
}
