package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.port.in.AddMissionHeartCommand;
import kr.co.deepple.heart.application.port.in.AddMissionHeartUseCase;
import kr.co.deepple.heart.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddMissionHeartService implements AddMissionHeartUseCase {

  private final HeartTransactor heartTransactor;

  @Override
  @Transactional
  public void addMissionHeart(AddMissionHeartCommand command) {
    heartTransactor.transact(
        command.memberId(),
        TransactionType.MISSION,
        command.actionType(),
        command.amount(),
        command.idempotencyKey());
  }
}
