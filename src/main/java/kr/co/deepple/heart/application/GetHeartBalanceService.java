package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.dto.HeartBalanceView;
import kr.co.deepple.heart.application.port.in.GetHeartBalanceUseCase;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.domain.MemberHeartBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetHeartBalanceService implements GetHeartBalanceUseCase {

  private final MemberHeartBalanceRepository memberHeartBalanceRepository;

  @Override
  @Transactional(readOnly = true)
  public HeartBalanceView getHeartBalance(long memberId) {
    return memberHeartBalanceRepository
        .findByMemberId(memberId)
        .map(this::toView)
        .orElseGet(HeartBalanceView::empty);
  }

  private HeartBalanceView toView(MemberHeartBalance balance) {
    return new HeartBalanceView(
        balance.getPurchaseBalance(), balance.getMissionBalance(), balance.getTotalBalance());
  }
}
