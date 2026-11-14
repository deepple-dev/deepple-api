package kr.co.deepple.member.application.port.in;

import java.util.Set;

public interface SearchIntroducibleMemberIdsUseCase {
  Set<Long> searchMemberIds(MemberSearchCondition condition, long limit);
}
