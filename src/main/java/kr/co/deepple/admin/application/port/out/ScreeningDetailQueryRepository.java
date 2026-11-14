package kr.co.deepple.admin.application.port.out;

import java.util.Optional;
import kr.co.deepple.admin.application.dto.RawScreeningDetailView;

public interface ScreeningDetailQueryRepository {
  Optional<RawScreeningDetailView> findById(long screeningId);
}
