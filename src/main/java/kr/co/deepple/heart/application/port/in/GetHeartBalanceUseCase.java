package kr.co.deepple.heart.application.port.in;

import kr.co.deepple.heart.application.dto.HeartBalanceView;

public interface GetHeartBalanceUseCase {
  HeartBalanceView getHeartBalance(long memberId);
}
