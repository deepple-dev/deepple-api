package kr.co.deepple.member.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface FindMemberSummaryUseCase {
  Optional<MemberSummary> findMemberSummary(long memberId);

  MemberSummary getMemberSummary(long memberId);

  Map<Long, MemberSummary> findMemberSummaries(Collection<Long> memberIds);
}
