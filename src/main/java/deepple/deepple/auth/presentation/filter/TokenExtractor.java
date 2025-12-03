package deepple.deepple.auth.presentation.filter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenExtractor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String COOKIE_NAME = "refresh_token";

    public String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header == null) {
            log.warn("Authorization 헤더가 없음: uri={}", request.getRequestURI());
            return null;
        }

        if (!header.startsWith(BEARER_PREFIX)) {
            log.warn("Authorization 헤더가 Bearer로 시작하지 않음: uri={}, header={}",
                request.getRequestURI(), header.substring(0, Math.min(header.length(), 20)));
            return null;
        }

        return header.substring(BEARER_PREFIX.length()).trim();
    }

    public String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
