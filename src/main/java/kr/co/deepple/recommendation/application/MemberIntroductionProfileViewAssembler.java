package kr.co.deepple.recommendation.application;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.member.application.port.in.FindMemberProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import kr.co.deepple.recommendation.application.port.out.IntroductionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberIntroductionProfileViewAssembler {

  private final FindMemberProfilesUseCase findMemberProfilesUseCase;
  private final FindLikeLevelUseCase findLikeLevelUseCase;
  private final IntroductionQueryRepository introductionQueryRepository;

  public List<MemberIntroductionProfileView> assemble(long memberId, Collection<Long> memberIds) {
    if (memberIds.isEmpty()) {
      return List.of();
    }
    Map<Long, MemberProfileSummary> profiles =
        findMemberProfilesUseCase.findMemberProfiles(memberIds);
    if (profiles.isEmpty()) {
      return List.of();
    }
    Map<Long, String> likeLevels = findLikeLevelUseCase.findLikeLevels(memberId, profiles.keySet());
    Set<Long> introducedMemberIds =
        introductionQueryRepository.findIntroducedMemberIds(memberId, profiles.keySet());

    return profiles.values().stream()
        .sorted(Comparator.comparingLong(MemberProfileSummary::memberId).reversed())
        .map(profile ->
            new MemberIntroductionProfileView(
                profile.memberId(),
                profile.profileImageUrl(),
                profile.age(),
                profile.nickname(),
                profile.city(),
                profile.district(),
                profile.hobbies().stream().toList(),
                profile.mbti(),
                profile.religion(),
                likeLevels.get(profile.memberId()),
                introducedMemberIds.contains(profile.memberId())))
        .toList();
  }
}
