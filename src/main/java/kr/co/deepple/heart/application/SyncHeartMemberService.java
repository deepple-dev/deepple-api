package kr.co.deepple.heart.application;

import java.time.LocalDateTime;
import kr.co.deepple.heart.application.port.in.SyncHeartMemberCommand;
import kr.co.deepple.heart.application.port.in.SyncHeartMemberUseCase;
import kr.co.deepple.heart.application.port.out.HeartMemberRepository;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.domain.Gender;
import kr.co.deepple.heart.domain.HeartMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncHeartMemberService implements SyncHeartMemberUseCase {

  private final HeartMemberRepository heartMemberRepository;
  private final MemberHeartBalanceRepository memberHeartBalanceRepository;

  @Override
  @Transactional
  public void syncHeartMember(SyncHeartMemberCommand command) {
    long memberId = command.memberId();
    Gender gender = command.gender() != null ? Gender.valueOf(command.gender()) : null;
    LocalDateTime now = LocalDateTime.now();

    heartMemberRepository.insertIfAbsent(
        memberId, command.gender(), command.isVip(), command.sourceUpdatedAt(), now);
    HeartMember heartMember =
        heartMemberRepository
            .findByMemberIdForUpdate(memberId)
            .orElseThrow(() -> new IllegalStateException("하트 회원 사본이 없습니다. memberId=" + memberId));
    heartMember.update(gender, command.isVip(), command.sourceUpdatedAt());
    memberHeartBalanceRepository.insertIfAbsent(memberId, now);
  }
}
