package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.dto.AdminHeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionView;
import kr.co.deepple.heart.application.port.in.FindAdminHeartTransactionsUseCase;
import kr.co.deepple.heart.application.port.out.AdminHeartTransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindAdminHeartTransactionsService implements FindAdminHeartTransactionsUseCase {

  private final AdminHeartTransactionQueryRepository adminHeartTransactionQueryRepository;

  @Override
  public Page<AdminHeartTransactionView> findAdminHeartTransactions(
      AdminHeartTransactionSearchCondition condition, Pageable pageable) {
    return adminHeartTransactionQueryRepository.findPage(condition, pageable);
  }
}
