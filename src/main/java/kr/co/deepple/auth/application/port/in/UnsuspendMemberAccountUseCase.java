package kr.co.deepple.auth.application.port.in;

public interface UnsuspendMemberAccountUseCase {
  void unsuspend(long memberId);
}
