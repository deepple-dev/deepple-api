package kr.co.deepple.member.command.application.member;

import java.time.LocalDateTime;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberDeleteScheduler {

  private final MemberCommandRepository memberCommandRepository;

  @Transactional
  @Scheduled(cron = "${scheduler.cron}")
  public void delete() {
    LocalDateTime now = LocalDateTime.now();
    memberCommandRepository.deleteBefore(now.minusMonths(3));
  }
}
