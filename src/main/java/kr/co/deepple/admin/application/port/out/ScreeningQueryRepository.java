package kr.co.deepple.admin.application.port.out;

import java.util.Set;
import kr.co.deepple.admin.application.dto.RawScreeningView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScreeningQueryRepository {
  Page<RawScreeningView> findScreenings(
      String screeningStatus, Set<Long> memberIds, Pageable pageable);
}
