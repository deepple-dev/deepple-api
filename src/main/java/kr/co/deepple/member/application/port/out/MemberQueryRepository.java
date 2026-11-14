package kr.co.deepple.member.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.member.application.dto.MemberContactView;
import kr.co.deepple.member.application.dto.MemberProfileView;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberIdSearchCondition;
import kr.co.deepple.member.application.port.in.MemberPageItem;
import kr.co.deepple.member.application.port.in.MemberPageSearchCondition;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import kr.co.deepple.member.application.port.in.MemberScreeningProfile;
import kr.co.deepple.member.application.port.in.MemberSearchCondition;
import kr.co.deepple.member.domain.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberQueryRepository {
  Optional<MemberProfileView> findProfileByMemberId(long memberId);

  Optional<MemberContactView> findContactsByMemberId(long memberId);

  Optional<MemberDetail> findDetailByMemberId(long memberId);

  Page<MemberPageItem> findPage(MemberPageSearchCondition condition, Pageable pageable);

  List<MemberPreview> findPreviewsByMemberIds(Collection<Long> memberIds);

  List<MemberProfileSummary> findProfilesByMemberIds(Collection<Long> memberIds);

  Set<Long> findIdsByNicknameAndPhoneNumber(String nicknameKeyword, String phoneNumber);

  Set<Long> searchIds(MemberIdSearchCondition condition);

  List<MemberScreeningProfile> findScreeningProfilesByMemberIds(Collection<Long> memberIds);

  List<Long> findIntroducibleIds(
      Collection<Long> candidateIds, Gender gender, long excludedMemberId);

  Set<Long> searchIntroducibleIds(MemberSearchCondition condition, long limit);
}
