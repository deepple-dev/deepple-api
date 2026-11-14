package kr.co.deepple.member.application;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.domain.MemberProfile;
import kr.co.deepple.member.domain.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMemberSummaryService implements FindMemberSummaryUseCase {

  private final MemberCommandRepository memberCommandRepository;

  @Override
  public Optional<MemberSummary> findMemberSummary(long memberId) {
    return memberCommandRepository.findById(memberId).map(FindMemberSummaryService::toSummary);
  }

  @Override
  public MemberSummary getMemberSummary(long memberId) {
    return findMemberSummary(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }

  @Override
  public Map<Long, MemberSummary> findMemberSummaries(Collection<Long> memberIds) {
    return memberCommandRepository.findAllById(new HashSet<>(memberIds)).stream()
        .map(FindMemberSummaryService::toSummary)
        .collect(Collectors.toMap(MemberSummary::memberId, Function.identity()));
  }

  private static MemberSummary toSummary(Member member) {
    MemberProfile profile = member.getProfile();
    Region region = profile == null ? null : profile.getRegion();
    return new MemberSummary(
        member.getId(),
        profile == null || profile.getNickname() == null ? null : profile.getNickname().getValue(),
        profile == null || profile.getGender() == null ? null : profile.getGender().name(),
        profile == null || profile.getYearOfBirth() == null
            ? null
            : profile.getYearOfBirth().getAge(),
        region == null || region.getCity() == null ? null : region.getCity().name(),
        region == null || region.getDistrict() == null ? null : region.getDistrict().name(),
        member.getGrade() == null ? null : member.getGrade().name(),
        member.getActivityStatus() == null ? null : member.getActivityStatus().name(),
        member.isVip(),
        member.hasSubmittedDatingExam());
  }
}
