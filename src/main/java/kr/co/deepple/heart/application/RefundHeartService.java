package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.port.in.RefundHeartCommand;
import kr.co.deepple.heart.application.port.in.RefundHeartUseCase;
import kr.co.deepple.heart.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundHeartService implements RefundHeartUseCase {

  private final HeartTransactor heartTransactor;

  @Override
  @Transactional
  public void refundHeart(RefundHeartCommand command) {
    heartTransactor.transact(
        command.memberId(),
        TransactionType.REFUND,
        TransactionType.REFUND.getDescription(),
        command.amount(),
        String.valueOf(command.orderId()));
  }
}
