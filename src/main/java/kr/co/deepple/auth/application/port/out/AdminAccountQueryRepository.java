package kr.co.deepple.auth.application.port.out;

import java.util.List;
import kr.co.deepple.auth.application.dto.PendingAdminView;

public interface AdminAccountQueryRepository {
  List<PendingAdminView> findPendingAdmins();
}
