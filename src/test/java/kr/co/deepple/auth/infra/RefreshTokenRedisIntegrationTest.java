package kr.co.deepple.auth.infra;

import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.RefreshTokenService;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenRedisIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("발급한 토큰은 한 번 쓰면 사용 중 목록에서 빠지고, 30초 안의 재시도는 같은 주인으로 처리됩니다.")
    void consumeIssuedToken() {
        // given
        TokenOwner owner = TokenOwner.member(101L);
        String token = refreshTokenService.issue(owner);

        // when
        TokenOwner first = refreshTokenService.consume(token);
        TokenOwner retry = refreshTokenService.consume(token);

        // then
        assertThat(first).isEqualTo(owner);
        assertThat(retry).isEqualTo(owner);
    }

    @Test
    @DisplayName("같은 토큰으로 동시에 갱신해도 모두 같은 주인으로 처리됩니다.")
    void consumeConcurrently() {
        // given
        TokenOwner owner = TokenOwner.member(102L);
        String token = refreshTokenService.issue(owner);

        // when
        List<TokenOwner> owners = IntStream.range(0, 5)
            .mapToObj(i -> CompletableFuture.supplyAsync(() -> refreshTokenService.consume(token)))
            .toList()
            .stream()
            .map(CompletableFuture::join)
            .toList();

        // then
        assertThat(owners).containsOnly(owner);
    }

    @Test
    @DisplayName("사용자의 토큰을 모두 폐기하면 그 사용자의 다른 기기 토큰도 쓸 수 없습니다.")
    void revokeAll() {
        // given
        TokenOwner owner = new TokenOwner(103L, Role.ADMIN);
        String phone = refreshTokenService.issue(owner);
        String laptop = refreshTokenService.issue(owner);
        String otherMember = refreshTokenService.issue(TokenOwner.member(103L));

        // when
        refreshTokenService.revokeAll(owner);

        // then
        assertThatThrownBy(() -> refreshTokenService.consume(phone))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);
        assertThatThrownBy(() -> refreshTokenService.consume(laptop))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);
        assertThat(refreshTokenService.consume(otherMember)).isEqualTo(TokenOwner.member(103L));
    }

    @Test
    @DisplayName("로그아웃한 토큰은 쓸 수 없습니다.")
    void revoke() {
        // given
        String token = refreshTokenService.issue(TokenOwner.member(104L));

        // when
        refreshTokenService.revoke(token);

        // then
        assertThatThrownBy(() -> refreshTokenService.consume(token))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}
