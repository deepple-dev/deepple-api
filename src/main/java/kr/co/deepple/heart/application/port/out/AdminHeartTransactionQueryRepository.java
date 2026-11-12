package kr.co.deepple.heart.application.port.out;

import kr.co.deepple.heart.application.dto.AdminHeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminHeartTransactionQueryRepository {
  Page<AdminHeartTransactionView> findPage(
      AdminHeartTransactionSearchCondition condition, Pageable pageable);
}
