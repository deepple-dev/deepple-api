package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.port.in.AddPurchaseHeartCommand;
import kr.co.deepple.heart.application.port.in.AddPurchaseHeartUseCase;
import kr.co.deepple.heart.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddPurchaseHeartService implements AddPurchaseHeartUseCase {

  private final HeartTransactor heartTransactor;

  @Override
  @Transactional
  public void addPurchaseHeart(AddPurchaseHeartCommand command) {
    heartTransactor.transact(
        command.memberId(),
        TransactionType.PURCHASE,
        TransactionType.PURCHASE.getDescription(),
        command.amount(),
        String.valueOf(command.orderId()));
  }
}
