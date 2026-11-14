package kr.co.deepple.heart.application;

import java.util.Set;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionView;
import kr.co.deepple.heart.application.port.in.FindAdminHeartTransactionsUseCase;
import kr.co.deepple.heart.application.port.out.AdminHeartTransactionQueryRepository;
import kr.co.deepple.member.application.port.in.FindMemberIdsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindAdminHeartTransactionsService implements FindAdminHeartTransactionsUseCase {

  private final AdminHeartTransactionQueryRepository adminHeartTransactionQueryRepository;
  private final FindMemberIdsUseCase findMemberIdsUseCase;

  @Override
  public Page<AdminHeartTransactionView> findAdminHeartTransactions(
      AdminHeartTransactionSearchCondition condition, Pageable pageable) {
    return adminHeartTransactionQueryRepository.findPage(
        condition, findMemberIds(condition), pageable);
  }

  private Set<Long> findMemberIds(AdminHeartTransactionSearchCondition condition) {
    if (condition.nickname() == null && condition.phoneNumber() == null) {
      return null;
    }
    return findMemberIdsUseCase.findMemberIds(condition.nickname(), condition.phoneNumber());
  }
}
