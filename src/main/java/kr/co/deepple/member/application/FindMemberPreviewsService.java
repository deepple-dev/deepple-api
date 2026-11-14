package kr.co.deepple.member.application;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.application.port.out.MemberQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindMemberPreviewsService implements FindMemberPreviewsUseCase {

  private final MemberQueryRepository memberQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public Map<Long, MemberPreview> findMemberPreviews(Collection<Long> memberIds) {
    if (memberIds.isEmpty()) {
      return Map.of();
    }
    return memberQueryRepository.findPreviewsByMemberIds(memberIds).stream()
        .collect(
            Collectors.toMap(
                MemberPreview::memberId, Function.identity(), (first, second) -> first));
  }
}
