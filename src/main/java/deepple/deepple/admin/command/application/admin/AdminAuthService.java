package deepple.deepple.admin.command.application.admin;

import deepple.deepple.admin.command.domain.admin.*;
import deepple.deepple.admin.presentation.admin.dto.AdminLoginRequest;
import deepple.deepple.admin.presentation.admin.dto.AdminLoginResponse;
import deepple.deepple.admin.presentation.admin.dto.AdminSignupRequest;
import deepple.deepple.admin.presentation.admin.dto.AdminSignupResponse;
import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.auth.domain.TokenRepository;
import deepple.deepple.common.enums.Role;
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
    private final TokenRepository tokenRepository;

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

        Role role = admin.getTokenRole();
        Instant issuedAt = Instant.now();
        String accessToken = tokenProvider.createAccessToken(admin.getId(), role, issuedAt);
        String refreshToken = tokenProvider.createRefreshToken(admin.getId(), role, issuedAt);
        tokenRepository.save(refreshToken);

        return AdminAuthMapper.toLoginResponse(accessToken, refreshToken);
    }

    public void logout(String refreshToken) {
        tokenRepository.delete(refreshToken);
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
