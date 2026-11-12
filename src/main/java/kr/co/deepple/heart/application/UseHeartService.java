package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.port.in.UseHeartCommand;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.heart.application.port.out.HeartMemberRepository;
import kr.co.deepple.heart.application.port.out.HeartUsagePolicyRepository;
import kr.co.deepple.heart.domain.HeartMember;
import kr.co.deepple.heart.domain.HeartUsagePolicy;
import kr.co.deepple.heart.domain.TransactionSubtype;
import kr.co.deepple.heart.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UseHeartService implements UseHeartUseCase {

  private final HeartMemberRepository heartMemberRepository;
  private final HeartUsagePolicyRepository heartUsagePolicyRepository;
  private final HeartTransactor heartTransactor;

  @Override
  @Transactional
  public void useHeart(UseHeartCommand command) {
    TransactionType transactionType = TransactionType.valueOf(command.usageType());
    TransactionSubtype subtype = TransactionSubtype.valueOf(command.usageSubtype());
    HeartMember heartMember = getHeartMember(command.memberId());
    HeartUsagePolicy policy = getUsagePolicy(heartMember, transactionType);
    if (policy.isFree(heartMember.isVip(), subtype)) {
      return;
    }

    String content =
        command.content() != null ? command.content() : transactionType.getDescription();
    heartTransactor.transact(
        command.memberId(), transactionType, content, policy.getPrice(), command.idempotencyKey());
  }

  private HeartMember getHeartMember(long memberId) {
    return heartMemberRepository
        .findByMemberId(memberId)
        .orElseThrow(() -> new IllegalStateException("하트 회원 사본이 없습니다. memberId=" + memberId));
  }

  private HeartUsagePolicy getUsagePolicy(
      HeartMember heartMember, TransactionType transactionType) {
    return heartUsagePolicyRepository
        .findByGenderAndTransactionType(heartMember.getGender(), transactionType)
        .orElseThrow(() ->
            new IllegalStateException(
                "하트 사용 정책이 없습니다. gender="
                    + heartMember.getGender()
                    + ", transactionType="
                    + transactionType));
  }
}
