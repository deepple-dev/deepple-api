package deepple.deepple.auth.presentation.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PathMatcherConfig {

    private static final List<String> EXCLUDED_URIS = List.of(
        "/member/login", "/member/logout", "/member/code", "/member/login/test", "/member/refresh",
        "/admin/login", "/admin/signup", "/admin/logout", "/member/profile/active",
        "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**",
        "/webjars/**",
        "/actuator/**"
    );

    @Bean
    public PathMatcherHelper pathMatcherHelper() {
        return new PathMatcherHelper(EXCLUDED_URIS);
    }
}
