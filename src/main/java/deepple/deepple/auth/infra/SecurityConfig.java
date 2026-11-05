package deepple.deepple.auth.infra;

import deepple.deepple.auth.presentation.CorsProperties;
import deepple.deepple.auth.presentation.TokenAccessDeniedHandler;
import deepple.deepple.auth.presentation.TokenAuthenticationEntryPoint;
import deepple.deepple.common.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final RequestMatcher[] PUBLIC_REQUESTS = {
        post("/member/login"),
        post("/member/login/test"),
        post("/member/code"),
        post("/member/refresh"),
        post("/member/profile/active"),
        post("/admin/login"),
        post("/admin/signup"),
        post("/admin/logout"),
        post("/payment/app-store/notifications"),
        get("/v3/api-docs/**"),
        get("/swagger-ui.html"),
        get("/swagger-ui/**"),
        get("/swagger-resources/**"),
        get("/webjars/**")
    };
    private static final String ADMIN_ACCOUNT_API = "/admin/accounts/**";
    private static final String ADMIN_API = "/admin/**";
    private static final String ROLE_PREFIX = "ROLE_";

    private final TokenAuthenticationEntryPoint authenticationEntryPoint;
    private final TokenAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        return applyCommonSettings(http)
            .securityMatchers(matchers -> matchers
                .requestMatchers(PUBLIC_REQUESTS)
                .requestMatchers(EndpointRequest.toAnyEndpoint()))
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        return applyCommonSettings(http)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(ADMIN_ACCOUNT_API).hasRole(Role.SUPER_ADMIN.name())
                .requestMatchers(ADMIN_API).hasAnyRole(Role.ADMIN.name(), Role.SUPER_ADMIN.name())
                .anyRequest().hasRole(Role.MEMBER.name()))
            .oauth2ResourceServer(resourceServer -> resourceServer
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(List.of(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name()
        ));
        configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
        configuration.setExposedHeaders(List.of(HttpHeaders.AUTHORIZATION));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static RequestMatcher post(String path) {
        return PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.POST, path);
    }

    private static RequestMatcher get(String path) {
        return PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.GET, path);
    }

    private HttpSecurity applyCommonSettings(HttpSecurity http) throws Exception {
        return http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName(JwtSpec.ROLES_CLAIM);
        authoritiesConverter.setAuthorityPrefix(ROLE_PREFIX);

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }
}
