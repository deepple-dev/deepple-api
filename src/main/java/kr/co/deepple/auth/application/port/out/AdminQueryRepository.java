package kr.co.deepple.auth.application.port.out;

import java.util.List;
import kr.co.deepple.auth.application.dto.AdminSearchCondition;
import kr.co.deepple.auth.application.dto.AdminView;

public interface AdminQueryRepository {
  List<AdminView> findAdmins(AdminSearchCondition condition);
}
