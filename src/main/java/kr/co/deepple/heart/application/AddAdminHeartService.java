package kr.co.deepple.heart.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.heart.application.port.in.AddAdminHeartCommand;
import kr.co.deepple.heart.application.port.in.AddAdminHeartUseCase;
import kr.co.deepple.heart.application.port.out.HeartMemberRepository;
import kr.co.deepple.heart.domain.HeartErrorCode;
import kr.co.deepple.heart.domain.TransactionType;
import kr.co.deepple.heart.event.AdminHeartGrantedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddAdminHeartService implements AddAdminHeartUseCase {

  private final HeartMemberRepository heartMemberRepository;
  private final HeartTransactor heartTransactor;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public void addAdminHeart(AddAdminHeartCommand command) {
    if (!heartMemberRepository.existsByMemberId(command.memberId())) {
      throw new DeeppleException(HeartErrorCode.MEMBER_NOT_FOUND, "memberId=" + command.memberId());
    }
    boolean transacted =
        heartTransactor.transact(
            command.memberId(),
            TransactionType.ADMIN_GRANT,
            command.reason(),
            command.amount(),
            command.idempotencyKey());
    if (!transacted) {
      return;
    }
    log.atInfo()
        .addKeyValue("memberId", command.memberId())
        .addKeyValue("amount", command.amount())
        .log("관리자 하트 지급");
    eventPublisher.publishEvent(
        AdminHeartGrantedEvent.of(command.memberId(), command.adminId(), command.amount()));
  }
}
