package kr.co.deepple.member.application.port.in;

public interface ChangeMemberScreeningStatusUseCase {
  void approveScreening(long memberId);

  void changeReject(long memberId);

  void changeWaiting(long memberId);
}
