package kr.co.deepple.member.application.port.in;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FindMemberScreeningProfilesUseCase {
  Map<Long, MemberScreeningProfile> findMemberScreeningProfiles(Collection<Long> memberIds);

  List<MemberProfileImage> findProfileImages(long memberId);
}
