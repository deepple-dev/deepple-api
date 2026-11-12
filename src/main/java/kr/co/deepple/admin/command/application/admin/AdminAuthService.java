package kr.co.deepple.admin.command.application.admin;

import kr.co.deepple.admin.command.domain.admin.*;
import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.presentation.admin.dto.AdminLoginRequest;
import kr.co.deepple.admin.presentation.admin.dto.AdminLoginResponse;
import kr.co.deepple.admin.presentation.admin.dto.AdminSignupRequest;
import kr.co.deepple.admin.presentation.admin.dto.AdminSignupResponse;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.RefreshTokenService;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.domain.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
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
        log.atInfo().addKeyValue("adminId", newAdmin.getId()).log("관리자 가입 신청");
        return AdminAuthMapper.toSignupResponse(newAdmin);
    }

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        Admin admin = findAdminByEmail(request.email());
        admin.matchPassword(request.password(), passwordHasher);
        validateApproved(admin);

        log.atInfo().addKeyValue("adminId", admin.getId()).log("관리자 로그인");
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
            .ifPresent(admin -> {
                throw new DeeppleException(AdminErrorCode.ADMIN_EMAIL_ALREADY_EXISTS);
            });
    }

    private Admin createAdmin(AdminSignupRequest request) {
        Password password = Password.fromRaw(request.password(), passwordHasher);
        Admin newAdmin = AdminAuthMapper.toAdmin(request, password);
        return adminCommandRepository.save(newAdmin);
    }

    private Admin findAdminByEmail(String email) {
        return adminCommandRepository.findByEmail(Email.from(email))
            .filter(admin -> !admin.isDeleted())
            .orElseThrow(() -> new DeeppleException(AdminErrorCode.INVALID_CREDENTIALS, "존재하지 않는 관리자입니다."));
    }

    private void validateApproved(Admin admin) {
        if (!admin.isApproved()) {
            throw new DeeppleException(AdminErrorCode.ADMIN_NOT_APPROVED, "adminId=" + admin.getId());
        }
    }
}
