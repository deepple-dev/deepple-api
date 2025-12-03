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
            log.warn("[토큰 추출 실패] Authorization 헤더 없음, uri={}", request.getRequestURI());
            return null;
        }

        if (!header.startsWith(BEARER_PREFIX)) {
            log.warn("[토큰 추출 실패] Bearer 형식 아님, uri={}", request.getRequestURI());
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
