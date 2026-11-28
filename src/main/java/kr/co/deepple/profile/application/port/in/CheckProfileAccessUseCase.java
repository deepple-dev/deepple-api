package kr.co.deepple.profile.application.port.in;

public interface CheckProfileAccessUseCase {

  /** 회원이 상대 프로필을 조회(`GET /member/{otherMemberId}`)할 수 있는지. 프로필 조회의 접근 권한 규칙과 같다. */
  boolean canViewProfile(long memberId, long otherMemberId);
}
