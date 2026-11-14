package kr.co.deepple.match.application;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.match.application.dto.MatchHistorySearchCondition;
import kr.co.deepple.match.application.dto.MatchHistoryView;
import kr.co.deepple.match.application.dto.RawMatchHistoryView;
import kr.co.deepple.match.application.port.out.AdminMatchHistoryQueryRepository;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.MemberIdSearchCondition;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.application.port.in.SearchMemberIdsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MatchHistoryQueryService {

  private final AdminMatchHistoryQueryRepository adminMatchHistoryQueryRepository;
  private final SearchMemberIdsUseCase searchMemberIdsUseCase;
  private final FindMemberPreviewsUseCase findMemberPreviewsUseCase;

  public Page<MatchHistoryView> findMatchHistories(
      MatchHistorySearchCondition condition, Pageable pageable) {
    Set<Long> nicknameMemberIds = null;
    if (hasText(condition.nickname())) {
      nicknameMemberIds =
          searchMemberIdsUseCase.searchMemberIds(
              new MemberIdSearchCondition(condition.nickname(), null, null, null));
      if (nicknameMemberIds.isEmpty()) {
        return Page.empty(pageable);
      }
    }
    Set<Long> phoneNumberMemberIds = null;
    if (hasText(condition.phoneNumber())) {
      phoneNumberMemberIds =
          searchMemberIdsUseCase.searchMemberIds(
              new MemberIdSearchCondition(null, condition.phoneNumber(), null, null));
      if (phoneNumberMemberIds.isEmpty()) {
        return Page.empty(pageable);
      }
    }

    Page<RawMatchHistoryView> page =
        adminMatchHistoryQueryRepository.findMatchHistories(
            condition, nicknameMemberIds, phoneNumberMemberIds, pageable);
    Set<Long> memberIds = new HashSet<>();
    for (RawMatchHistoryView raw : page.getContent()) {
      memberIds.add(raw.requesterId());
      memberIds.add(raw.responderId());
    }
    Map<Long, MemberPreview> members = findMemberPreviewsUseCase.findMemberPreviews(memberIds);
    return page.map(raw ->
        new MatchHistoryView(
            raw.matchId(),
            nicknameOf(members.get(raw.requesterId())),
            nicknameOf(members.get(raw.responderId())),
            raw.requestMessage(),
            raw.responseMessage(),
            raw.status(),
            raw.readByResponderAt()));
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private String nicknameOf(MemberPreview member) {
    return member == null ? null : member.nickname();
  }
}
