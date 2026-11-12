package kr.co.deepple.common.security;

import kr.co.deepple.common.enums.Role;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import io.sentry.spring.boot4.SentryAutoConfiguration;
import io.sentry.spring7.HttpServletRequestSentryUserProvider;
import io.sentry.spring7.SentryUserFilter;
import io.sentry.spring7.SpringSecuritySentryUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MemberSentryUserProviderTest {

    private final MemberSentryUserProvider provider = new MemberSentryUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        Sentry.close();
    }

    @Test
    @DisplayName("회원 토큰이면 회원 ID를 Sentry 사용자 ID로 넘긴다.")
    void provideMemberId() {
        authenticate("7", JwtSpec.authorityOf(Role.MEMBER));

        User user = provider.provideUser();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo("7");
        assertThat(user.getIpAddress()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    @DisplayName("관리자 토큰이면 사용자 정보를 넘기지 않는다.")
    void ignoreAdmin() {
        authenticate("3", JwtSpec.authorityOf(Role.ADMIN));

        assertThat(provider.provideUser()).isNull();
    }

    @Test
    @DisplayName("인증 정보가 없으면 사용자 정보를 넘기지 않는다.")
    void ignoreUnauthenticated() {
        assertThat(provider.provideUser()).isNull();
    }

    @Test
    @DisplayName("JWT 인증이 아니면 사용자 정보를 넘기지 않는다.")
    void ignoreNonJwtAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("7", null, AuthorityUtils.createAuthorityList(JwtSpec.authorityOf(Role.MEMBER))));
        assertThat(provider.provideUser()).isNull();
    }

    @Test
    @DisplayName("Sentry 자동 설정의 사용자 필터를 거치면 회원 ID만 기록되고 IP·사용자 이름은 없다.")
    void recordOnlyMemberIdThroughSentryUserFilter() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SentryAutoConfiguration.class))
            .withBean(MemberSentryUserProvider.class)
            .withPropertyValues("sentry.dsn=https://public@localhost/1", "sentry.data-collection.user-info=false")
            .run(context -> {
                authenticate("7", JwtSpec.authorityOf(Role.MEMBER));
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.setRemoteAddr("203.0.113.10");
                request.addHeader("X-Forwarded-For", "198.51.100.20");

                FilterRegistrationBean<?> registration = context.getBean("sentryUserFilter", FilterRegistrationBean.class);
                assertThat(registration.getFilter()).isInstanceOf(SentryUserFilter.class);
                assertThat(((SentryUserFilter) registration.getFilter()).getSentryUserProviders())
                    .hasExactlyElementsOfTypes(HttpServletRequestSentryUserProvider.class,
                        SpringSecuritySentryUserProvider.class, MemberSentryUserProvider.class);
                registration.getFilter().doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

                AtomicReference<User> user = new AtomicReference<>();
                Sentry.configureScope(scope -> user.set(scope.getUser()));
                assertThat(user.get().getId()).isEqualTo("7");
                assertThat(user.get().getIpAddress()).isNull();
                assertThat(user.get().getUsername()).isNull();
            });
    }

    private void authenticate(String subject, String authority) {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject(subject)
            .build();
        SecurityContextHolder.getContext()
            .setAuthentication(new JwtAuthenticationToken(jwt, AuthorityUtils.createAuthorityList(authority)));
    }
}
