package deepple.deepple.admin.command.application.admin;

import deepple.deepple.admin.command.domain.admin.*;
import deepple.deepple.admin.presentation.admin.dto.AdminLoginRequest;
import deepple.deepple.admin.presentation.admin.dto.AdminLoginResponse;
import deepple.deepple.admin.presentation.admin.dto.AdminSignupRequest;
import deepple.deepple.admin.presentation.admin.dto.AdminSignupResponse;
import deepple.deepple.auth.domain.AuthErrorCode;
import deepple.deepple.auth.domain.RefreshTokenService;
import deepple.deepple.auth.domain.TokenOwner;
import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.exception.DeeppleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminCommandRepository adminCommandRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AdminSignupResponse signup(AdminSignupRequest request) {
        validateEmailUniqueness(request.email());
        Admin newAdmin = createAdmin(request);
        return AdminAuthMapper.toSignupResponse(newAdmin);
    }

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = findAdminByEmail(request.email());
        admin.matchPassword(request.password(), passwordHasher);
        validateApproved(admin);

        return issueTokens(admin);
    }

    @Transactional(readOnly = true)
    public AdminLoginResponse refresh(String refreshToken) {
        TokenOwner owner = refreshTokenService.consume(refreshToken);
        if (!owner.isAdmin()) {
            throw new DeeppleException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Admin admin = adminCommandRepository.findById(owner.id())
            .filter(found -> !found.isDeleted() && found.isApproved())
            .orElse(null);
        if (admin == null) {
            refreshTokenService.revokeAll(owner);
            throw new DeeppleException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        return issueTokens(admin);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private AdminLoginResponse issueTokens(Admin admin) {
        Role role = admin.getTokenRole();
        String accessToken = tokenProvider.createAccessToken(admin.getId(), role, Instant.now());
        String refreshToken = refreshTokenService.issue(new TokenOwner(admin.getId(), role));
        return AdminAuthMapper.toLoginResponse(accessToken, refreshToken);
    }

    private void validateEmailUniqueness(String email) {
        adminCommandRepository.findByEmail(Email.from(email))
            .ifPresent(admin -> {throw new DuplicateEmailException();});
    }

    private Admin createAdmin(AdminSignupRequest request) {
        Password password = Password.fromRaw(request.password(), passwordHasher);
        Admin newAdmin = AdminAuthMapper.toAdmin(request, password);
        return adminCommandRepository.save(newAdmin);
    }

    private Admin findAdminByEmail(String email) {
        return adminCommandRepository.findByEmail(Email.from(email))
            .filter(admin -> !admin.isDeleted())
            .orElseThrow(AdminNotFoundException::new);
    }

    private void validateApproved(Admin admin) {
        if (!admin.isApproved()) {
            throw new AdminNotApprovedException();
        }
    }
}
