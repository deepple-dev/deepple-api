package kr.co.deepple.member.application.port.in;

import java.util.Set;

public interface SearchMemberIdsUseCase {
  Set<Long> searchMemberIds(MemberIdSearchCondition condition);
}
