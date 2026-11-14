package kr.co.deepple.admin.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.admin.adapter.out.persistence.ScreeningDetailQueryRepository;
import kr.co.deepple.admin.adapter.out.persistence.ScreeningQueryRepository;
import kr.co.deepple.admin.application.dto.RawScreeningDetailView;
import kr.co.deepple.admin.application.dto.RawScreeningView;
import kr.co.deepple.admin.application.dto.ScreeningDetailInterviewView;
import kr.co.deepple.admin.application.dto.ScreeningDetailProfileImageView;
import kr.co.deepple.admin.application.dto.ScreeningDetailProfileView;
import kr.co.deepple.admin.application.dto.ScreeningDetailView;
import kr.co.deepple.admin.application.dto.ScreeningSearchCondition;
import kr.co.deepple.admin.application.dto.ScreeningView;
import kr.co.deepple.admin.domain.AdminErrorCode;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.application.port.in.FindMemberInterviewAnswersUseCase;
import kr.co.deepple.member.application.port.in.FindMemberScreeningProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberIdSearchCondition;
import kr.co.deepple.member.application.port.in.MemberScreeningProfile;
import kr.co.deepple.member.application.port.in.SearchMemberIdsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScreeningQueryService {

  private static final long MISSING_VERSION = -1L;
  private static final DateTimeFormatter JOINED_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

  private final ScreeningQueryRepository screeningQueryRepository;
  private final ScreeningDetailQueryRepository screeningDetailQueryRepository;
  private final SearchMemberIdsUseCase searchMemberIdsUseCase;
  private final FindMemberScreeningProfilesUseCase findMemberScreeningProfilesUseCase;
  private final FindMemberInterviewAnswersUseCase findMemberInterviewAnswersUseCase;

  public Page<ScreeningView> findScreenings(ScreeningSearchCondition condition, Pageable pageable) {
    MemberIdSearchCondition memberCondition = toMemberCondition(condition);
    Set<Long> memberIds = null;
    if (!memberCondition.isEmpty()) {
      memberIds = searchMemberIdsUseCase.searchMemberIds(memberCondition);
      if (memberIds.isEmpty()) {
        return Page.empty(pageable);
      }
    }

    Page<RawScreeningView> page =
        screeningQueryRepository.findScreenings(condition.screeningStatus(), memberIds, pageable);
    Map<Long, MemberScreeningProfile> members =
        findMemberScreeningProfilesUseCase.findMemberScreeningProfiles(
            page.getContent().stream().map(RawScreeningView::memberId).collect(Collectors.toSet()));
    return page.map(raw -> {
      MemberScreeningProfile member = members.get(raw.memberId());
      return new ScreeningView(
          raw.screeningId(),
          member == null ? null : member.nickname(),
          member == null ? null : member.gender(),
          member == null ? null : formatJoinedDate(member.joinedAt()),
          raw.screeningStatus(),
          raw.rejectionReason());
    });
  }

  public ScreeningDetailView getScreeningDetail(long screeningId) {
    RawScreeningDetailView raw =
        screeningDetailQueryRepository
            .findById(screeningId)
            .orElseThrow(() ->
                new DeeppleException(
                    AdminErrorCode.SCREENING_NOT_FOUND, "screeningId=" + screeningId));
    long memberId = raw.memberId();
    MemberScreeningProfile member =
        findMemberScreeningProfilesUseCase
            .findMemberScreeningProfiles(Set.of(memberId))
            .get(memberId);
    if (member == null) {
      throw new DeeppleException(
          AdminErrorCode.SCREENING_NOT_FOUND,
          "screeningId=" + screeningId + ", memberId=" + memberId);
    }
    return new ScreeningDetailView(
        screeningId,
        raw.version() == null ? MISSING_VERSION : raw.version(),
        new ScreeningDetailProfileView(
            memberId,
            raw.screeningStatus(),
            raw.rejectionReason(),
            member.nickname(),
            member.yearOfBirth(),
            member.gender(),
            formatJoinedDate(member.joinedAt())),
        findMemberScreeningProfilesUseCase.findProfileImages(memberId).stream()
            .map(image ->
                new ScreeningDetailProfileImageView(
                    image.imageUrl(), image.order(), image.primary()))
            .toList(),
        findMemberInterviewAnswersUseCase.findPublicInterviewAnswers(memberId).stream()
            .map(answer -> new ScreeningDetailInterviewView(answer.question(), answer.answer()))
            .toList());
  }

  private MemberIdSearchCondition toMemberCondition(ScreeningSearchCondition condition) {
    return new MemberIdSearchCondition(
        blankToNull(condition.nickname()),
        blankToNull(condition.phoneNumber()),
        condition.startDate() == null ? null : condition.startDate().atStartOfDay(),
        condition.endDate() == null
            ? null
            : condition.endDate().plusDays(1).atStartOfDay().minusSeconds(1));
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private String formatJoinedDate(LocalDateTime joinedAt) {
    return joinedAt == null ? null : joinedAt.format(JOINED_DATE_FORMAT);
  }
}
