package kr.co.deepple.member.command.application.member;

import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.co.deepple.admin.command.domain.suspension.Suspension;
import kr.co.deepple.admin.command.domain.suspension.SuspensionCommandRepository;
import kr.co.deepple.admin.command.domain.suspension.SuspensionStatus;
import kr.co.deepple.admin.fixture.SuspensionFixture;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.RefreshTokenService;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.infra.JwtProvider;
import kr.co.deepple.common.MockEventsExtension;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.application.member.dto.MemberLoginServiceDto;
import kr.co.deepple.member.command.application.member.dto.TokenPairResponse;
import kr.co.deepple.member.command.application.member.sms.AuthMessageService;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.command.domain.member.event.MemberLoggedInEvent;
import kr.co.deepple.member.fixture.MemberFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith({MockitoExtension.class, MockEventsExtension.class})
class MemberAuthServiceTest {

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private SuspensionCommandRepository suspensionRepository;

  @Mock private JwtProvider jwtProvider;

  @Mock private RefreshTokenService refreshTokenService;

  @Mock private AuthMessageService authMessageService;

  @InjectMocks private MemberAuthService memberAuthService;

  @Nested
  @DisplayName("로그인 및 회원가입 테스트")
  class Login {

    private Member permanentStoppedMember;
    private Member member;
    private Long memberId;

    @BeforeEach
    void setUp() {
      memberId = 1L;
      member = MemberFixture.builder().id(memberId).phoneNumber("01012345678").noProfile().build();

      permanentStoppedMember =
          MemberFixture.builder()
              .id(2L)
              .phoneNumber("01012345678")
              .activityStatus(ActivityStatus.SUSPENDED_PERMANENTLY)
              .noProfile()
              .build();
    }

    @Test
    @DisplayName("일시 정지된 사용자가 로그인할 경우 예외 처리")
    void shouldThrowExceptionWhenLoginAttemptedByTemporarilyStoppedMember() {
      // Given
      memberId = 100L;
      String phoneNumber = "01012341234";
      String code = "012345";

      Member temporarySuspensionMember =
          MemberFixture.builder()
              .id(memberId)
              .phoneNumber(phoneNumber)
              .activityStatus(ActivityStatus.SUSPENDED_TEMPORARILY)
              .build();

      Instant suspensionExpireAt = Instant.now().plus(3, java.time.temporal.ChronoUnit.DAYS);

      Suspension suspension =
          SuspensionFixture.builder()
              .memberId(memberId)
              .status(SuspensionStatus.TEMPORARY)
              .expireAt(suspensionExpireAt)
              .build();

      when(memberCommandRepository.findByPhoneNumber(phoneNumber))
          .thenReturn(Optional.of(temporarySuspensionMember));
      Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);
      when(suspensionRepository.findByMemberIdAndStatusOrderByExpireAtDesc(
              memberId, SuspensionStatus.TEMPORARY))
          .thenReturn(Optional.of(suspension));

      // When
      Exception exception =
          Assertions.catchException(() -> memberAuthService.login(phoneNumber, code));

      // Then
      Assertions.assertThat(exception)
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_TEMPORARILY_SUSPENDED);
      DeeppleException suspensionException = (DeeppleException) exception;
      Assertions.assertThat(suspensionException.getProperties().get("suspensionExpireAt"))
          .isEqualTo(LocalDateTime.ofInstant(suspensionExpireAt, java.time.ZoneId.systemDefault()));
    }

    @Test
    @DisplayName("영구 정지된 사용자가 로그인할 경우 예외 처리")
    void shouldThrowExceptionWhenLoginAttemptedByPermanentStoppedMember() {
      // Given
      String phoneNumber = "01012345678";
      String code = "01012345678";

      when(memberCommandRepository.findByPhoneNumber(phoneNumber))
          .thenReturn(Optional.of(permanentStoppedMember));
      Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

      // When & Then
      Assertions.assertThatThrownBy(() -> memberAuthService.login(phoneNumber, code))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_PERMANENTLY_SUSPENDED);
    }

    @Test
    @DisplayName("회원 탈퇴한 사용자가 로그인할 경우 예외 처리")
    void shouldThrowExceptionWhenLoginAttemptedByDeletedMember() {
      // Given
      String phoneNumber = "01012345678";
      String code = "01012345678";
      Member deletedMember = MemberFixture.builder().phoneNumber(phoneNumber).build();
      deletedMember.delete();

      when(memberCommandRepository.findByPhoneNumber(phoneNumber))
          .thenReturn(Optional.of(deletedMember));
      Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

      // When & Then
      Assertions.assertThatThrownBy(() -> memberAuthService.login(phoneNumber, code))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_DELETED);
    }

    @Test
    @DisplayName("휴면 회원도 로그인하고, 응답에 휴면 상태를 담으며 로그인 이벤트는 발행하지 않는다")
    void loginDormantMember(MockedStatic<Events> events) {
      // Given
      String phoneNumber = "01012345678";
      String code = "01012345678";
      member =
          MemberFixture.builder()
              .id(memberId)
              .phoneNumber(phoneNumber)
              .activityStatus(ActivityStatus.DORMANT)
              .noProfile()
              .build();

      when(memberCommandRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(member));
      Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

      // When
      MemberLoginServiceDto response = memberAuthService.login(phoneNumber, code);

      // Then
      Assertions.assertThat(response.activityStatus()).isEqualTo(ActivityStatus.DORMANT.name());
      Mockito.verify(refreshTokenService).issue(TokenOwner.member(memberId));
      events.verify(() -> Events.raise(Mockito.any(MemberLoggedInEvent.class)), Mockito.never());
    }

    @Test
    @DisplayName("활동 중인 회원이 로그인하면 로그인 이벤트를 발행한다")
    void raiseLoggedInEventForActiveMember(MockedStatic<Events> events) {
      // Given
      String phoneNumber = "01012345678";
      String code = "01012345678";

      when(memberCommandRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(member));
      Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

      // When
      memberAuthService.login(phoneNumber, code);

      // Then
      events.verify(() -> Events.raise(Mockito.any(MemberLoggedInEvent.class)));
    }

    @Test
    @DisplayName("기존 유저가 로그인할 경우, 토큰 발급")
    void shouldCreateTokenWhenUserIsRegistered() {
      String phoneNumber = "01012345678";
      String code = "01012345678";
      Instant fixedInstant = Instant.parse("2024-01-01T00:00:00Z");

      try (MockedStatic<Instant> mockedInstant =
          Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
        mockedInstant.when(Instant::now).thenReturn(fixedInstant);

        when(memberCommandRepository.findByPhoneNumber(phoneNumber))
            .thenReturn(Optional.of(member));
        when(jwtProvider.createAccessToken(
                Mockito.anyLong(), Mockito.eq(Role.MEMBER), Mockito.eq(fixedInstant)))
            .thenReturn("accessToken");
        Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

        // When
        String token = memberAuthService.login(phoneNumber, code).accessToken();

        // Then
        Assertions.assertThat(token).isNotNull();
        Assertions.assertThat(token).isEqualTo("accessToken");
      }
    }

    @Test
    @DisplayName("등록되지 않은 유저가 로그인할 경우, 새롭게 생성하여 토큰 발급")
    void shouldCreateNewMemberAndTokenWhenUserIsNotRegistered() {
      // Given
      String phoneNumber = "01012345678";
      String code = "01012345678";
      Instant fixedInstant = Instant.parse("2024-01-01T00:00:00Z");

      try (MockedStatic<Instant> mockedInstant =
          Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
        mockedInstant.when(Instant::now).thenReturn(fixedInstant);

        when(memberCommandRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());
        when(jwtProvider.createAccessToken(
                Mockito.anyLong(), Mockito.eq(Role.MEMBER), Mockito.eq(fixedInstant)))
            .thenReturn("accessToken");
        when(memberCommandRepository.save(Mockito.any())).thenReturn(member);
        Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

        // When
        MemberLoginServiceDto response = memberAuthService.login(phoneNumber, code);

        // Then
        Assertions.assertThat(response.accessToken()).isNotNull();
        Assertions.assertThat(response.accessToken()).isEqualTo("accessToken");
        Assertions.assertThat(response.isProfileSettingNeeded()).isTrue();
      }
    }

    @Test
    @DisplayName("동시 로그인으로 인해, 유니크 제약조건에 걸린 경우 예외 반환")
    void shouldThrowExceptionWhenUniqueConstraint() {
      // Given
      String phoneNumber = "01012345678";
      String code = "01012345678";

      // When
      when(memberCommandRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());
      when(memberCommandRepository.save(Mockito.any()))
          .thenThrow(DataIntegrityViolationException.class);
      Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

      // When & Then
      Assertions.assertThatThrownBy(() -> memberAuthService.login(phoneNumber, code))
          .isInstanceOf(ConcurrencyFailureException.class);
    }

    @Test
    @DisplayName("로그인 성공 시 마지막 접속 시각을 갱신한다")
    void updatesLastAccessedAtOnLogin() {
      String phoneNumber = "01012345678";
      String code = "01012345678";
      Instant fixedInstant = Instant.parse("2024-01-01T00:00:00Z");

      try (MockedStatic<Instant> mockedInstant =
          Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
        mockedInstant.when(Instant::now).thenReturn(fixedInstant);

        when(memberCommandRepository.findByPhoneNumber(phoneNumber))
            .thenReturn(Optional.of(member));
        when(jwtProvider.createAccessToken(
                Mockito.anyLong(), Mockito.eq(Role.MEMBER), Mockito.eq(fixedInstant)))
            .thenReturn("accessToken");
        Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

        // When
        memberAuthService.login(phoneNumber, code);

        // Then
        Mockito.verify(memberCommandRepository)
            .updateLastAccessedAt(Mockito.eq(memberId), Mockito.any(LocalDateTime.class));
      }
    }
  }

  @Nested
  @DisplayName("토큰 갱신 테스트")
  class Refresh {

    private static final String REFRESH_TOKEN = "refreshToken";

    @Test
    @DisplayName("토큰 갱신 성공 시 새 토큰을 발급하고 마지막 접속 시각을 갱신한다")
    void updatesLastAccessedAtOnRefresh() {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).build();
      when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(TokenOwner.member(memberId));
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));
      when(jwtProvider.createAccessToken(
              Mockito.eq(memberId), Mockito.eq(Role.MEMBER), Mockito.any()))
          .thenReturn("newAccessToken");
      when(refreshTokenService.issue(TokenOwner.member(memberId))).thenReturn("newRefreshToken");

      // When
      TokenPairResponse response = memberAuthService.refresh(REFRESH_TOKEN);

      // Then
      Assertions.assertThat(response.refreshToken()).isEqualTo("newRefreshToken");
      Mockito.verify(memberCommandRepository)
          .updateLastAccessedAt(Mockito.eq(memberId), Mockito.any(LocalDateTime.class));
    }

    @Test
    @DisplayName("정지된 회원이 갱신하면 예외가 발생하고 모든 리프레시 토큰을 폐기한다")
    void revokeAllWhenSuspendedMemberRefreshes() {
      // Given
      long memberId = 2L;
      Member member =
          MemberFixture.builder()
              .id(memberId)
              .activityStatus(ActivityStatus.SUSPENDED_PERMANENTLY)
              .build();
      when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(TokenOwner.member(memberId));
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When & Then
      Assertions.assertThatThrownBy(() -> memberAuthService.refresh(REFRESH_TOKEN))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_PERMANENTLY_SUSPENDED);
      Mockito.verify(refreshTokenService).revokeAll(TokenOwner.member(memberId));
      Mockito.verify(refreshTokenService, Mockito.never()).issue(Mockito.any());
    }

    @Test
    @DisplayName("휴면 회원도 토큰을 갱신할 수 있다")
    void refreshDormantMember() {
      // Given
      long memberId = 3L;
      Member member =
          MemberFixture.builder().id(memberId).activityStatus(ActivityStatus.DORMANT).build();
      when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(TokenOwner.member(memberId));
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));
      when(refreshTokenService.issue(TokenOwner.member(memberId))).thenReturn("newRefreshToken");

      // When
      TokenPairResponse response = memberAuthService.refresh(REFRESH_TOKEN);

      // Then
      Assertions.assertThat(response.refreshToken()).isEqualTo("newRefreshToken");
      Mockito.verify(refreshTokenService, Mockito.never()).revokeAll(Mockito.any());
    }

    @Test
    @DisplayName("관리자 리프레시 토큰으로 회원 갱신을 요청하면 예외가 발생한다")
    void rejectAdminRefreshToken() {
      // Given
      when(refreshTokenService.consume(REFRESH_TOKEN)).thenReturn(new TokenOwner(1L, Role.ADMIN));

      // When & Then
      Assertions.assertThatThrownBy(() -> memberAuthService.refresh(REFRESH_TOKEN))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);
      Mockito.verify(memberCommandRepository, Mockito.never()).findById(Mockito.anyLong());
    }
  }

  @Nested
  @DisplayName("회원 탈퇴 테스트.")
  class Delete {

    @DisplayName("회원으로 존재하지 않는 경우, 예외 발생.")
    @Test
    void throwExceptionWhenMemberIsNotFound() {
      // Given
      long memberId = 1L;
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(() -> memberAuthService.delete(memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @DisplayName("존재하는 경우, 회원을 SoftDelete 하고 모든 리프레시 토큰을 폐기한다.")
    @Test
    void softDeleteWhenMemberIsFound() {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberAuthService.delete(memberId);

      // Then
      Assertions.assertThat(member.isDeleted()).isTrue();
      Mockito.verify(refreshTokenService).revokeAll(TokenOwner.member(memberId));
    }
  }
}
