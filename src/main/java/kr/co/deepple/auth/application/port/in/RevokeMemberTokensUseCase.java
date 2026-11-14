package kr.co.deepple.auth.application.port.in;

public interface RevokeMemberTokensUseCase {
  void revokeAll(long memberId);
}
