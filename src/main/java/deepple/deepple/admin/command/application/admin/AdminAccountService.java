package deepple.deepple.admin.command.application.admin;

import deepple.deepple.admin.command.domain.admin.Admin;
import deepple.deepple.admin.command.domain.admin.AdminCommandRepository;
import deepple.deepple.admin.command.domain.admin.AdminRole;
import deepple.deepple.admin.command.domain.admin.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AdminCommandRepository adminCommandRepository;

    @Transactional
    public void approve(long adminId, String role) {
        AdminRole adminRole = AdminRole.from(role);
        Admin admin = getPendingAdmin(adminId);
        admin.approve(adminRole);
    }

    @Transactional
    public void reject(long adminId) {
        Admin admin = getPendingAdmin(adminId);
        adminCommandRepository.delete(admin);
    }

    private Admin getPendingAdmin(long adminId) {
        return adminCommandRepository.findByIdAndApprovalStatus(adminId, ApprovalStatus.PENDING)
            .orElseThrow(PendingAdminNotFoundException::new);
    }
}
