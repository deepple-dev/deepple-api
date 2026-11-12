package kr.co.deepple.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

public class RequestLogContextInterceptor implements HandlerInterceptor {

  private static final String AUTHORITY_PREFIX = "ROLE_";

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    MDC.put(LogContext.HTTP_REQUEST_METHOD, request.getMethod());
    Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    if (route != null) {
      MDC.put(LogContext.HTTP_ROUTE, route.toString());
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken)) {
      MDC.put(LogContext.USER_ID, authentication.getName());
      MDC.put(LogContext.USER_ROLES, toRoles(authentication));
    }
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    MDC.remove(LogContext.HTTP_REQUEST_METHOD);
    MDC.remove(LogContext.HTTP_ROUTE);
    MDC.remove(LogContext.USER_ID);
    MDC.remove(LogContext.USER_ROLES);
  }

  private String toRoles(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> authority.startsWith(AUTHORITY_PREFIX))
        .map(authority -> authority.substring(AUTHORITY_PREFIX.length()))
        .collect(Collectors.joining(","));
  }
}
