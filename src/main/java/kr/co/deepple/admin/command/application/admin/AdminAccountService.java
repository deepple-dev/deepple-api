package kr.co.deepple.admin.command.application.admin;

import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.command.domain.admin.Admin;
import kr.co.deepple.admin.command.domain.admin.AdminCommandRepository;
import kr.co.deepple.admin.command.domain.admin.AdminRole;
import kr.co.deepple.admin.command.domain.admin.ApprovalStatus;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAccountService {

  private final AdminCommandRepository adminCommandRepository;

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
    adminCommandRepository.delete(admin);
    log.atInfo().addKeyValue("targetAdminId", adminId).log("관리자 가입 거절");
  }

  private Admin getPendingAdmin(long adminId) {
    return adminCommandRepository
        .findByIdAndApprovalStatus(adminId, ApprovalStatus.PENDING)
        .orElseThrow(() ->
            new DeeppleException(AdminErrorCode.PENDING_ADMIN_NOT_FOUND, "adminId=" + adminId));
  }
}
