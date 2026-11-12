package kr.co.deepple.datingexam.application.port.in;

import java.util.List;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;

public interface PersonalityTypeMemberFinder {
  /**
   * 주어진 대표 성격 유형(dominantPersonalityType)을 가진 이성 회원 id를 최신 가입순으로 조회한다. 반대 성별 · 프로필 공개 · ACTIVE ·
   * 차단/피차단·본인 제외 필터가 적용된다.
   */
  List<Long> findMemberIdsByDominantType(Long memberId, AnswerPersonalityType type);
}
