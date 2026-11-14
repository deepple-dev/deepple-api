package kr.co.deepple.member.adapter.in.spi;

import kr.co.deepple.auth.application.port.out.spi.MemberRegistrar;
import kr.co.deepple.member.application.MemberAccountSyncService;
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
