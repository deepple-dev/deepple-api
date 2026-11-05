package deepple.deepple.auth.presentation;

import deepple.deepple.auth.infra.JwtSpec;
import deepple.deepple.common.enums.Role;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        Jwt jwt = jwtAuthentication.getToken();
        AuthContext authContext = new AuthContext();
        Role role = Role.valueOf(jwt.getClaimAsStringList(JwtSpec.ROLES_CLAIM)
            .getFirst());
        authContext.authenticate(Long.parseLong(jwt.getSubject()), role);
        return authContext;
    }
}
