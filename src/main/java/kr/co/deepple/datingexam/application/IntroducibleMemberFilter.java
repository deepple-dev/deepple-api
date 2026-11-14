package kr.co.deepple.datingexam.application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import kr.co.deepple.member.application.port.in.FilterIntroducibleMemberIdsUseCase;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntroducibleMemberFilter {

  private final FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final FilterIntroducibleMemberIdsUseCase filterIntroducibleMemberIdsUseCase;

  public List<Long> filter(long memberId, Set<Long> candidateIds) {
    Set<Long> remaining = new HashSet<>(candidateIds);
    remaining.removeAll(findBlockedMemberIdsUseCase.findBlockRelatedMemberIds(memberId));
    remaining.remove(memberId);
    if (remaining.isEmpty()) {
      return List.of();
    }

    String gender =
        findMemberSummaryUseCase
            .findMemberSummary(memberId)
            .map(MemberSummary::gender)
            .orElse(null);
    if (gender == null) {
      throw new DeeppleException(DatingExamErrorCode.GENDER_NOT_SET, "memberId=" + memberId);
    }
    return filterIntroducibleMemberIdsUseCase.filterIntroducibleMemberIds(memberId, remaining);
  }
}
