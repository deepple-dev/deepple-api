package kr.co.deepple.heart.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.heart.application.dto.AdminHeartGrantRequest;
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
  public void addAdminHeart(long adminId, AdminHeartGrantRequest request) {
    if (!heartMemberRepository.existsByMemberId(request.memberId())) {
      throw new DeeppleException(HeartErrorCode.MEMBER_NOT_FOUND, "memberId=" + request.memberId());
    }
    boolean transacted =
        heartTransactor.transact(
            request.memberId(),
            TransactionType.ADMIN_GRANT,
            request.reason(),
            request.amount(),
            request.idempotencyKey());
    if (!transacted) {
      return;
    }
    log.atInfo()
        .addKeyValue("memberId", request.memberId())
        .addKeyValue("amount", request.amount())
        .log("관리자 하트 지급");
    eventPublisher.publishEvent(
        AdminHeartGrantedEvent.of(request.memberId(), adminId, request.amount()));
  }
}
