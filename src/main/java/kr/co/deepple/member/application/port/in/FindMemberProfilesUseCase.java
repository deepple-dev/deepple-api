package kr.co.deepple.member.application.port.in;

import java.util.Collection;
import java.util.Map;

public interface FindMemberProfilesUseCase {
  Map<Long, MemberProfileSummary> findMemberProfiles(Collection<Long> memberIds);
}
