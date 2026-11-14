package kr.co.deepple.member.application.port.in;

public interface ChangeMemberActivityStatusUseCase {
  void changeMemberActivityStatus(long memberId, String status);
}
