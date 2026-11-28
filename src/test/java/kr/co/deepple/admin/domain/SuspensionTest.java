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

  @Test
  @DisplayName("isExpired(): 일시 정지는 만료 시각이 지나면 만료, 영구 정지는 만료되지 않는다")
  void isExpired() {
    var now = Instant.parse("2026-11-13T00:00:00Z");

    assertThat(SuspensionFixture.builder().expireAt(now.minusSeconds(1)).build().isExpired(now))
        .isTrue();
    assertThat(SuspensionFixture.builder().expireAt(now).build().isExpired(now)).isTrue();
    assertThat(SuspensionFixture.builder().expireAt(now.plusSeconds(1)).build().isExpired(now))
        .isFalse();
    assertThat(
            SuspensionFixture.builder().status(SuspensionStatus.PERMANENT).build().isExpired(now))
        .isFalse();
  }

  @Test
  @DisplayName("restartTemporaryIfExpired(): 만료된 일시 정지는 지금부터 다시 정지한다")
  void restartExpiredTemporary() {
    var now = Instant.parse("2026-11-13T00:00:00Z");
    var suspension = SuspensionFixture.builder().adminId(1L).expireAt(now.minusSeconds(1)).build();

    boolean restarted = suspension.restartTemporaryIfExpired(2L, now);

    assertThat(restarted).isTrue();
    assertThat(suspension.getAdminId()).isEqualTo(2L);
    assertThat(suspension.getExpireAt())
        .isEqualTo(now.plus(SuspensionPolicy.TEMPORARY_SUSPENSION_DURATION));
  }

  @Test
  @DisplayName("restartTemporaryIfExpired(): 만료되지 않은 일시 정지와 영구 정지는 바꾸지 않는다")
  void doNotRestartWhenNotExpired() {
    var now = Instant.parse("2026-11-13T00:00:00Z");
    var expireAt = now.plusSeconds(1);
    var active = SuspensionFixture.builder().adminId(1L).expireAt(expireAt).build();
    var permanent = SuspensionFixture.builder().status(SuspensionStatus.PERMANENT).build();

    assertThat(active.restartTemporaryIfExpired(2L, now)).isFalse();
    assertThat(active.getAdminId()).isEqualTo(1L);
    assertThat(active.getExpireAt()).isEqualTo(expireAt);
    assertThat(permanent.restartTemporaryIfExpired(2L, now)).isFalse();
    assertThat(permanent.getExpireAt()).isNull();
  }
}
