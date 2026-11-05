package deepple.deepple.auth.domain;

import deepple.deepple.auth.infra.RefreshTokenProperties;
import deepple.deepple.auth.infra.RefreshTokenRepository;
import deepple.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final RefreshTokenProperties PROPERTIES = new RefreshTokenProperties(Duration.ofDays(30), null);
    private static final TokenOwner OWNER = TokenOwner.member(1L);

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, PROPERTIES);
    }

    @Test
    @DisplayName("사용 중인 토큰을 쓰면 사용 기록을 남긴 뒤 삭제하고 주인을 돌려줍니다.")
    void consumeActiveToken() {
        // given
        when(refreshTokenRepository.findOwner(anyString())).thenReturn(Optional.of(OWNER));
        when(refreshTokenRepository.markUsed(anyString(), any(), any(), any())).thenReturn(true);

        // when
        TokenOwner owner = refreshTokenService.consume("token");

        // then
        assertThat(owner).isEqualTo(OWNER);
        verify(refreshTokenRepository).delete(anyString(), any());
    }

    @Test
    @DisplayName("30초 안에 다시 쓴 토큰은 재시도로 보고 주인을 돌려줍니다.")
    void allowReuseWithinGracePeriod() {
        // given
        when(refreshTokenRepository.findOwner(anyString())).thenReturn(Optional.empty());
        when(refreshTokenRepository.findUsed(anyString()))
            .thenReturn(Optional.of(new UsedRefreshToken(OWNER, Instant.now().minusSeconds(10))));

        // when
        TokenOwner owner = refreshTokenService.consume("token");

        // then
        assertThat(owner).isEqualTo(OWNER);
        verify(refreshTokenRepository, never()).deleteAll(any());
    }

    @Test
    @DisplayName("동시에 요청해 사용 기록을 먼저 남기지 못하면 재시도로 처리합니다.")
    void allowConcurrentRequestThatLostRace() {
        // given
        when(refreshTokenRepository.findOwner(anyString())).thenReturn(Optional.of(OWNER));
        when(refreshTokenRepository.markUsed(anyString(), any(), any(), any())).thenReturn(false);
        when(refreshTokenRepository.findUsed(anyString()))
            .thenReturn(Optional.of(new UsedRefreshToken(OWNER, Instant.now())));

        // when
        TokenOwner owner = refreshTokenService.consume("token");

        // then
        assertThat(owner).isEqualTo(OWNER);
        verify(refreshTokenRepository, never()).delete(anyString(), any());
    }

    @Test
    @DisplayName("30초가 지난 뒤 다시 쓴 토큰은 탈취로 보고 그 사용자의 모든 토큰을 폐기합니다.")
    void revokeAllWhenReusedAfterGracePeriod() {
        // given
        when(refreshTokenRepository.findOwner(anyString())).thenReturn(Optional.empty());
        when(refreshTokenRepository.findUsed(anyString()))
            .thenReturn(Optional.of(new UsedRefreshToken(OWNER, Instant.now().minusSeconds(31))));

        // when & then
        assertThatThrownBy(() -> refreshTokenService.consume("token"))
            .isInstanceOf(DeeppleException.class)
            .extracting("errorCode")
            .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
        verify(refreshTokenRepository).deleteAll(OWNER);
    }

    @Test
    @DisplayName("저장소에 없는 토큰은 유효하지 않습니다.")
    void rejectUnknownToken() {
        // given
        when(refreshTokenRepository.findOwner(anyString())).thenReturn(Optional.empty());
        when(refreshTokenRepository.findUsed(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.consume("token"))
            .isInstanceOf(DeeppleException.class)
            .extracting("errorCode")
            .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("토큰이 없으면 MISSING_REFRESH_TOKEN 오류가 발생합니다.")
    void rejectMissingToken(String token) {
        // when & then
        assertThatThrownBy(() -> refreshTokenService.consume(token))
            .isInstanceOf(DeeppleException.class)
            .extracting("errorCode")
            .isEqualTo(AuthErrorCode.MISSING_REFRESH_TOKEN);
    }
}
