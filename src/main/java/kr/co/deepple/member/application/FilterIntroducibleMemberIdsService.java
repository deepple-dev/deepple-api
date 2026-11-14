package kr.co.deepple.member.application;

import java.util.Collection;
import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.FilterIntroducibleMemberIdsUseCase;
import kr.co.deepple.member.application.port.out.MemberQueryRepository;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FilterIntroducibleMemberIdsService implements FilterIntroducibleMemberIdsUseCase {

  private final MemberRepository memberRepository;
  private final MemberQueryRepository memberQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Long> filterIntroducibleMemberIds(long memberId, Collection<Long> candidateIds) {
    if (candidateIds.isEmpty()) {
      return List.of();
    }
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() ->
                new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    Gender gender = member.getProfile() == null ? null : member.getProfile().getGender();
    if (gender == null) {
      throw new IllegalStateException("성별이 없는 회원입니다. memberId=" + memberId);
    }
    return memberQueryRepository.findIntroducibleIds(candidateIds, gender.getOpposite(), memberId);
  }
}
