package kr.co.deepple.member.command.infra.member;

import kr.co.deepple.auth.application.port.out.spi.MemberRegistrar;
import kr.co.deepple.member.command.application.member.MemberAccountSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMemberRegistrar implements MemberRegistrar {

  private final MemberAccountSyncService memberAccountSyncService;

  @Override
  public void register(long accountId, String phoneNumber) {
    memberAccountSyncService.register(accountId, phoneNumber);
  }
}
