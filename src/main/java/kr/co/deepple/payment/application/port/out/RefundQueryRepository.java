package kr.co.deepple.payment.application.port.out;

import kr.co.deepple.payment.application.dto.RefundSearchCondition;
import kr.co.deepple.payment.application.dto.RefundView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefundQueryRepository {
  Page<RefundView> findPage(RefundSearchCondition condition, Pageable pageable);
}
