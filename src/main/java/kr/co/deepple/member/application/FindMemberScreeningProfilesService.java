package kr.co.deepple.member.application;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.member.adapter.out.persistence.MemberQueryRepository;
import kr.co.deepple.member.adapter.out.persistence.ProfileImageQueryRepository;
import kr.co.deepple.member.application.port.in.FindMemberScreeningProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.member.application.port.in.MemberScreeningProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindMemberScreeningProfilesService implements FindMemberScreeningProfilesUseCase {

  private final MemberQueryRepository memberQueryRepository;
  private final ProfileImageQueryRepository profileImageQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public Map<Long, MemberScreeningProfile> findMemberScreeningProfiles(Collection<Long> memberIds) {
    if (memberIds.isEmpty()) {
      return Map.of();
    }
    return memberQueryRepository.findScreeningProfilesByMemberIds(memberIds).stream()
        .collect(Collectors.toMap(MemberScreeningProfile::memberId, Function.identity()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<MemberProfileImage> findProfileImages(long memberId) {
    return profileImageQueryRepository.findByMemberId(memberId).stream()
        .map(image -> new MemberProfileImage(image.url(), image.order(), image.isPrimary()))
        .toList();
  }
}
