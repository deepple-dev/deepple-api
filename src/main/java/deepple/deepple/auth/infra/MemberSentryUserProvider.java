package deepple.deepple.auth.infra;

import deepple.deepple.common.enums.Role;
import io.sentry.protocol.User;
import io.sentry.spring7.SentryUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class MemberSentryUserProvider implements SentryUserProvider {

    private static final String MEMBER_AUTHORITY = JwtSpec.authorityOf(Role.MEMBER);

    @Override
    public User provideUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken token) || !isMember(token)) {
            return null;
        }
        User user = new User();
        user.setId(token.getName());
        return user;
    }

    private boolean isMember(Authentication authentication) {
        return AuthorityUtils.authorityListToSet(authentication.getAuthorities()).contains(MEMBER_AUTHORITY);
    }
}
