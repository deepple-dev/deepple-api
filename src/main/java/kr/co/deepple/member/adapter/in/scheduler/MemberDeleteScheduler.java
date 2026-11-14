package kr.co.deepple.member.adapter.in.scheduler;

import java.time.LocalDateTime;
import kr.co.deepple.member.application.port.out.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberDeleteScheduler {

  private final MemberRepository memberRepository;

  @Transactional
  @Scheduled(cron = "${scheduler.cron}")
  public void delete() {
    LocalDateTime now = LocalDateTime.now();
    memberRepository.deleteBefore(now.minusMonths(3));
  }
}
