package kr.co.deepple.community.application.port.out;

import java.util.Set;
import kr.co.deepple.community.application.dto.AdminSelfIntroductionSearchCondition;
import kr.co.deepple.community.application.dto.RawAdminSelfIntroductionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminSelfIntroductionQueryRepository {
  Page<RawAdminSelfIntroductionView> findSelfIntroductions(
      AdminSelfIntroductionSearchCondition condition, Set<Long> memberIds, Pageable pageable);
}
