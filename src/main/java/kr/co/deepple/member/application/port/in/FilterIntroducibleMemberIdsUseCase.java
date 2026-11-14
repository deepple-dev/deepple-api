package kr.co.deepple.member.application.port.in;

import java.util.Collection;
import java.util.List;

public interface FilterIntroducibleMemberIdsUseCase {
  List<Long> filterIntroducibleMemberIds(long memberId, Collection<Long> candidateIds);
}
