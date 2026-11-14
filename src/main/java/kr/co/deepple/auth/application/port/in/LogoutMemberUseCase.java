package kr.co.deepple.auth.application.port.in;

public interface LogoutMemberUseCase {
  void logout(long memberId, String refreshToken);
}
