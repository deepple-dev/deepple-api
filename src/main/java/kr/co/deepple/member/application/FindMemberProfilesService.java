package kr.co.deepple.member.application;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.member.adapter.out.persistence.MemberQueryRepository;
import kr.co.deepple.member.application.port.in.FindMemberProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindMemberProfilesService implements FindMemberProfilesUseCase {

  private final MemberQueryRepository memberQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public Map<Long, MemberProfileSummary> findMemberProfiles(Collection<Long> memberIds) {
    if (memberIds.isEmpty()) {
      return Map.of();
    }
    return memberQueryRepository.findProfilesByMemberIds(memberIds).stream()
        .collect(Collectors.toMap(MemberProfileSummary::memberId, Function.identity()));
  }
}
