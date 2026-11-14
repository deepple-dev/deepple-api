package kr.co.deepple.auth.application;

import kr.co.deepple.auth.application.port.out.AdminRepository;
import kr.co.deepple.auth.domain.Admin;
import kr.co.deepple.auth.domain.AdminRole;
import kr.co.deepple.auth.domain.ApprovalStatus;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAccountService {

  private final AdminRepository adminRepository;

  @Transactional
  public void approve(long adminId, String role) {
    AdminRole adminRole = AdminRole.from(role);
    Admin admin = getPendingAdmin(adminId);
    admin.approve(adminRole);
    log.atInfo()
        .addKeyValue("targetAdminId", adminId)
        .addKeyValue("role", adminRole)
        .log("관리자 가입 승인");
  }

  @Transactional
  public void reject(long adminId) {
    Admin admin = getPendingAdmin(adminId);
    adminRepository.delete(admin);
    log.atInfo().addKeyValue("targetAdminId", adminId).log("관리자 가입 거절");
  }

  private Admin getPendingAdmin(long adminId) {
    return adminRepository
        .findByIdAndApprovalStatus(adminId, ApprovalStatus.PENDING)
        .orElseThrow(() ->
            new DeeppleException(AuthErrorCode.PENDING_ADMIN_NOT_FOUND, "adminId=" + adminId));
  }
}
