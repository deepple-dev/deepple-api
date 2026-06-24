package deepple.deepple.member.command.application.member;

import deepple.deepple.admin.command.domain.suspension.Suspension;
import deepple.deepple.admin.command.domain.suspension.SuspensionCommandRepository;
import deepple.deepple.admin.command.domain.suspension.SuspensionStatus;
import deepple.deepple.auth.domain.TokenParser;
import deepple.deepple.auth.domain.TokenRepository;
import deepple.deepple.auth.infra.JwtProvider;
import deepple.deepple.common.MockEventsExtension;
import deepple.deepple.common.enums.Role;
import deepple.deepple.member.command.application.member.dto.MemberLoginServiceDto;
import deepple.deepple.member.command.application.member.exception.*;
import deepple.deepple.member.command.application.member.sms.AuthMessageService;
import deepple.deepple.member.command.domain.member.ActivityStatus;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.command.domain.member.exception.MemberNotActiveException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, MockEventsExtension.class})
class MemberAuthServiceTest {

    @Mock
    private MemberCommandRepository memberCommandRepository;

    @Mock
    private SuspensionCommandRepository suspensionRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenParser tokenParser;

    @Mock
    private AuthMessageService authMessageService;

    @InjectMocks
    private MemberAuthService memberAuthService;


    @Nested
    @DisplayName("로그인 및 회원가입 테스트")
    class Login {
        private Member permanentStoppedMember;
        private Member member;
        private Long memberId;

        @BeforeEach
        void setUp() {
            member = Member.fromPhoneNumber("01012345678");
            memberId = 1L;
            ReflectionTestUtils.setField(member, "id", memberId);
            ReflectionTestUtils.setField(member, "activityStatus", ActivityStatus.ACTIVE);

            permanentStoppedMember = Member.fromPhoneNumber("01012345678");
            ReflectionTestUtils.setField(permanentStoppedMember, "id", 2L);
            ReflectionTestUtils.setField(permanentStoppedMember, "activityStatus",
                ActivityStatus.SUSPENDED_PERMANENTLY);
        }

        @Test
        @DisplayName("일시 정지된 사용자가 로그인할 경우 예외 처리")
        void shouldThrowExceptionWhenLoginAttemptedByTemporarilyStoppedMember() {
            // Given
            memberId = 100L;
            String phoneNumber = "01012341234";
            String code = "012345";

            Member temporarySuspensionMember = mock(Member.class);
            when(temporarySuspensionMember.getId()).thenReturn(memberId);
            when(temporarySuspensionMember.isDeleted()).thenReturn(false);
            when(temporarySuspensionMember.getActivityStatus()).thenReturn(ActivityStatus.SUSPENDED_TEMPORARILY);

            Instant suspensionExpireAt = Instant.now().plus(3, java.time.temporal.ChronoUnit.DAYS);

            Suspension suspension = mock(Suspension.class);
            when(suspension.getExpireAt()).thenReturn(suspensionExpireAt);

            when(memberCommandRepository.findByPhoneNumber(phoneNumber))
                .thenReturn(Optional.of(temporarySuspensionMember));
            Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);
            when(suspensionRepository.findByMemberIdAndStatusOrderByExpireAtDesc(memberId, SuspensionStatus.TEMPORARY))
                .thenReturn(Optional.of(suspension));

            // When
            Exception exception = Assertions.catchException(
                () -> memberAuthService.login(phoneNumber, code)
            );

            // Then
            Assertions.assertThat(exception).isInstanceOf(TemporarilySuspendedMemberException.class);
            TemporarilySuspendedMemberException suspensionException = (TemporarilySuspendedMemberException) exception;
            Assertions.assertThat(suspensionException.getSuspensionExpireAt()).isEqualTo(
                LocalDateTime.ofInstant(suspensionExpireAt, java.time.ZoneId.systemDefault())
            );
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
                .isInstanceOf(PermanentlySuspendedMemberException.class);
        }

        @Test
        @DisplayName("회원 탈퇴한 사용자가 로그인할 경우 예외 처리")
        void shouldThrowExceptionWhenLoginAttemptedByDeletedMember() {
            // Given
            String phoneNumber = "01012345678";
            String code = "01012345678";
            Member deletedMember = Member.fromPhoneNumber("01012345678");
            deletedMember.delete();

            when(memberCommandRepository.findByPhoneNumber(phoneNumber))
                .thenReturn(Optional.of(deletedMember));
            Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

            // When & Then
            Assertions.assertThatThrownBy(() -> memberAuthService.login(phoneNumber, code))
                .isInstanceOf(MemberDeletedException.class);
        }

        @Test
        @DisplayName("활동 상태가 아닌 사용자가 로그인할 경우 예외 처리")
        void shouldThrowExceptionWhenLoginAttemptedByNotActiveMember() {
            // Given
            String phoneNumber = "01012345678";
            String code = "01012345678";
            Member inactiveMember = Member.fromPhoneNumber("01012345678");
            ReflectionTestUtils.setField(inactiveMember, "id", 3L);
            inactiveMember.changeToDormant();

            when(memberCommandRepository.findByPhoneNumber(phoneNumber))
                .thenReturn(Optional.of(inactiveMember));
            Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

            // When & Then
            Assertions.assertThatThrownBy(() -> memberAuthService.login(phoneNumber, code))
                .isInstanceOf(MemberNotActiveException.class);
        }

        @Test
        @DisplayName("기존 유저가 로그인할 경우, 토큰 발급")
        void shouldCreateTokenWhenUserIsRegistered() {
            String phoneNumber = "01012345678";
            String code = "01012345678";
            Instant fixedInstant = Instant.parse("2024-01-01T00:00:00Z");

            try (MockedStatic<Instant> mockedInstant = Mockito.mockStatic(Instant.class)) {
                mockedInstant.when(Instant::now).thenReturn(fixedInstant);

                when(memberCommandRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(member));
                when(
                    jwtProvider.createAccessToken(Mockito.anyLong(), Mockito.eq(Role.MEMBER), Mockito.eq(fixedInstant)))
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

            try (MockedStatic<Instant> mockedInstant = Mockito.mockStatic(Instant.class)) {
                mockedInstant.when(Instant::now).thenReturn(fixedInstant);

                when(memberCommandRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());
                when(
                    jwtProvider.createAccessToken(Mockito.anyLong(), Mockito.eq(Role.MEMBER), Mockito.eq(fixedInstant)))
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
            when(memberCommandRepository.save(Mockito.any())).thenThrow(DataIntegrityViolationException.class);
            Mockito.doNothing().when(authMessageService).authenticate(phoneNumber, code);

            // When & Then
            Assertions.assertThatThrownBy(() -> memberAuthService.login(phoneNumber, code))
                .isInstanceOf(MemberLoginConflictException.class);
        }

        @Test
        @DisplayName("로그인 성공 시 마지막 접속 시각을 갱신한다")
        void updatesLastAccessedAtOnLogin() {
            String phoneNumber = "01012345678";
            String code = "01012345678";
            Instant fixedInstant = Instant.parse("2024-01-01T00:00:00Z");

            try (MockedStatic<Instant> mockedInstant = Mockito.mockStatic(Instant.class)) {
                mockedInstant.when(Instant::now).thenReturn(fixedInstant);

                when(memberCommandRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(member));
                when(
                    jwtProvider.createAccessToken(Mockito.anyLong(), Mockito.eq(Role.MEMBER), Mockito.eq(fixedInstant)))
                    .thenReturn("accessToken");
                when(jwtProvider.createRefreshToken(Mockito.anyLong(), Mockito.eq(Role.MEMBER),
                    Mockito.eq(fixedInstant)))
                    .thenReturn("refreshToken");
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

        @Test
        @DisplayName("토큰 갱신 성공 시 마지막 접속 시각을 갱신한다")
        void updatesLastAccessedAtOnRefresh() {
            // Given
            String refreshToken = "refreshToken";
            long memberId = 1L;

            when(tokenParser.isExpired(refreshToken)).thenReturn(false);
            when(tokenParser.isValid(refreshToken)).thenReturn(true);
            when(tokenRepository.exists(refreshToken)).thenReturn(true);
            when(tokenParser.getId(refreshToken)).thenReturn(memberId);
            when(tokenParser.getRole(refreshToken)).thenReturn(Role.MEMBER);
            when(jwtProvider.createAccessToken(Mockito.eq(memberId), Mockito.eq(Role.MEMBER), Mockito.any()))
                .thenReturn("newAccessToken");
            when(jwtProvider.createRefreshToken(Mockito.eq(memberId), Mockito.eq(Role.MEMBER), Mockito.any()))
                .thenReturn("newRefreshToken");

            // When
            memberAuthService.refresh(refreshToken);

            // Then
            Mockito.verify(memberCommandRepository)
                .updateLastAccessedAt(Mockito.eq(memberId), Mockito.any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 테스트.")
    class Delete {
        @DisplayName("회원으로 존재하지 않는 경우, 예외 발생.")
        @Test
        void throwExceptionWhenMemberIsNotFound() {
            // Given
            Long memberId = 1L;
            String refreshToken = "refreshToken";
            when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

            // When & Then
            Assertions.assertThatThrownBy(() -> memberAuthService.delete(memberId, refreshToken))
                .isInstanceOf(MemberNotFoundException.class);
        }

        @DisplayName("존재하는 경우, 회원을 SoftDelete 한다.")
        @Test
        void softDeleteWhenMemberIsFound() {
            // Given
            Long memberId = 1L;
            String refreshToken = "refreshToken";
            Member member = Member.fromPhoneNumber("01012345678");
            when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

            // When
            memberAuthService.delete(memberId, refreshToken);

            // Then
            Assertions.assertThat(member.isDeleted()).isTrue();
        }
    }
}
