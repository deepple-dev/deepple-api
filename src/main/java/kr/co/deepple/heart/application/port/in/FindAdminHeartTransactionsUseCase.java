package kr.co.deepple.heart.application.port.in;

import kr.co.deepple.heart.application.dto.AdminHeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FindAdminHeartTransactionsUseCase {
  Page<AdminHeartTransactionView> findAdminHeartTransactions(
      AdminHeartTransactionSearchCondition condition, Pageable pageable);
}
