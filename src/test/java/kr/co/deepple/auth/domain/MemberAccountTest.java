package kr.co.deepple.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import kr.co.deepple.auth.fixture.MemberAccountFixture;
import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberAccountTest {

  private static final Instant NOW = Instant.parse("2026-11-13T00:00:00Z");

  @Test
  @DisplayName("전화번호로 계정을 만들면 정상 상태다.")
  void registerActiveAccount() {
    MemberAccount account = MemberAccount.register("01012345678");

    assertThat(account.getPhoneNumber().getValue()).isEqualTo("01012345678");
    assertThat(account.getStatus()).isEqualTo(MemberAccountStatus.ACTIVE);
    assertThatCode(() -> account.validateLoginAllowed(NOW)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("탈퇴한 계정은 로그인할 수 없다.")
  void rejectDeletedAccount() {
    MemberAccount account = MemberAccountFixture.builder().deleted(true).build();

    assertThatThrownBy(() -> account.validateLoginAllowed(NOW))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_DELETED);
  }

  @Test
  @DisplayName("영구 정지 계정은 로그인할 수 없다.")
  void rejectPermanentlySuspendedAccount() {
    MemberAccount account =
        MemberAccountFixture.builder().status(MemberAccountStatus.SUSPENDED_PERMANENTLY).build();

    assertThatThrownBy(() -> account.validateLoginAllowed(NOW))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_PERMANENTLY_SUSPENDED);
  }

  @Test
  @DisplayName("해제 시각이 지나지 않은 일시 정지 계정은 로그인할 수 없고 해제 시각을 알려 준다.")
  void rejectTemporarilySuspendedAccount() {
    Instant suspendedUntil = NOW.plus(3, ChronoUnit.DAYS);
    MemberAccount account =
        MemberAccountFixture.builder()
            .status(MemberAccountStatus.SUSPENDED_TEMPORARILY)
            .suspendedUntil(suspendedUntil)
            .build();

    assertThatThrownBy(() -> account.validateLoginAllowed(NOW))
        .isInstanceOfSatisfying(
            DeeppleException.class,
            e -> {
              assertThat(e.getErrorCode()).isEqualTo(AuthErrorCode.MEMBER_TEMPORARILY_SUSPENDED);
              assertThat(e.getProperties().get(MemberAccount.SUSPENSION_EXPIRE_AT_PROPERTY))
                  .isEqualTo(LocalDateTime.ofInstant(suspendedUntil, ZoneId.systemDefault()));
            });
  }

  @Test
  @DisplayName("해제 시각이 지난 일시 정지 계정은 로그인할 수 있다.")
  void allowAccountWhoseSuspensionExpired() {
    MemberAccount account =
        MemberAccountFixture.builder()
            .status(MemberAccountStatus.SUSPENDED_TEMPORARILY)
            .suspendedUntil(NOW.minus(1, ChronoUnit.SECONDS))
            .build();

    assertThatCode(() -> account.validateLoginAllowed(NOW)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("일시 정지하면 해제 시각과 함께 일시 정지 상태가 되고, 해제하면 정상 상태로 돌아온다.")
  void suspendTemporarilyAndUnsuspend() {
    MemberAccount account = MemberAccountFixture.builder().build();
    Instant suspendedUntil = NOW.plus(3, ChronoUnit.DAYS);

    account.suspendTemporarily(suspendedUntil);

    assertThat(account.getStatus()).isEqualTo(MemberAccountStatus.SUSPENDED_TEMPORARILY);
    assertThat(account.getSuspendedUntil()).isEqualTo(suspendedUntil);

    account.unsuspend();

    assertThat(account.getStatus()).isEqualTo(MemberAccountStatus.ACTIVE);
    assertThat(account.getSuspendedUntil()).isNull();
  }

  @Test
  @DisplayName("일시 정지를 영구 정지로 바꾸면 해제 시각이 사라진다.")
  void changeTemporarySuspensionToPermanent() {
    MemberAccount account =
        MemberAccountFixture.builder()
            .status(MemberAccountStatus.SUSPENDED_TEMPORARILY)
            .suspendedUntil(NOW.plus(3, ChronoUnit.DAYS))
            .build();

    account.suspendPermanently();

    assertThat(account.getStatus()).isEqualTo(MemberAccountStatus.SUSPENDED_PERMANENTLY);
    assertThat(account.getSuspendedUntil()).isNull();
    assertThatThrownBy(() -> account.validateLoginAllowed(NOW))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.MEMBER_PERMANENTLY_SUSPENDED);
  }
}
