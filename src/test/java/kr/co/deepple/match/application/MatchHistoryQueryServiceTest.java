package kr.co.deepple.match.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.match.adapter.out.persistence.AdminMatchHistoryQueryRepository;
import kr.co.deepple.match.application.dto.MatchHistorySearchCondition;
import kr.co.deepple.match.application.dto.MatchHistoryView;
import kr.co.deepple.match.application.dto.RawMatchHistoryView;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.MemberIdSearchCondition;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.application.port.in.SearchMemberIdsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MatchHistoryQueryServiceTest {

  private static final Pageable PAGEABLE = PageRequest.of(0, 10);
  private static final MemberIdSearchCondition NICKNAME_CONDITION =
      new MemberIdSearchCondition("요청자", null, null, null);
  private static final MemberIdSearchCondition PHONE_NUMBER_CONDITION =
      new MemberIdSearchCondition(null, "01022222222", null, null);

  @Mock private AdminMatchHistoryQueryRepository adminMatchHistoryQueryRepository;
  @Mock private SearchMemberIdsUseCase searchMemberIdsUseCase;
  @Mock private FindMemberPreviewsUseCase findMemberPreviewsUseCase;

  @InjectMocks private MatchHistoryQueryService matchHistoryQueryService;

  @Test
  @DisplayName("조건 없이 조회하면 회원을 검색하지 않고 요청자·응답자 닉네임을 붙여 돌려준다.")
  void findAllMatchHistories() {
    var condition = new MatchHistorySearchCondition(null, null, null, null, null);
    given(adminMatchHistoryQueryRepository.findMatchHistories(condition, null, null, PAGEABLE))
        .willReturn(
            new PageImpl<>(
                List.of(
                    new RawMatchHistoryView(
                        2L, 102L, 103L, "반갑습니다", "수락합니다", "MATCHED", "2026-01-01 10:00:00.000000"),
                    new RawMatchHistoryView(1L, 101L, 102L, "안녕하세요", null, "WAITING", null)),
                PAGEABLE,
                12));
    given(findMemberPreviewsUseCase.findMemberPreviews(Set.of(101L, 102L, 103L)))
        .willReturn(
            Map.of(
                101L,
                preview(101L, "요청자"),
                102L,
                preview(102L, "응답자"),
                103L,
                preview(103L, "다른회원")));

    Page<MatchHistoryView> matchHistories =
        matchHistoryQueryService.findMatchHistories(condition, PAGEABLE);

    assertThat(matchHistories.getTotalElements()).isEqualTo(12);
    assertThat(matchHistories.getPageable()).isEqualTo(PAGEABLE);
    assertThat(matchHistories.getContent())
        .containsExactly(
            new MatchHistoryView(
                2L, "응답자", "다른회원", "반갑습니다", "수락합니다", "MATCHED", "2026-01-01 10:00:00.000000"),
            new MatchHistoryView(1L, "요청자", "응답자", "안녕하세요", null, "WAITING", null));
    verifyNoInteractions(searchMemberIdsUseCase);
  }

  @Test
  @DisplayName("닉네임 조건은 닉네임이 일치하는 회원 번호로 바꿔 조회한다.")
  void findMatchHistoriesByNickname() {
    var condition = new MatchHistorySearchCondition(null, "요청자", null, null, null);
    given(searchMemberIdsUseCase.searchMemberIds(NICKNAME_CONDITION)).willReturn(Set.of(101L));
    given(
            adminMatchHistoryQueryRepository.findMatchHistories(
                condition, Set.of(101L), null, PAGEABLE))
        .willReturn(
            pageOf(new RawMatchHistoryView(1L, 101L, 102L, "안녕하세요", null, "WAITING", null)));
    given(findMemberPreviewsUseCase.findMemberPreviews(Set.of(101L, 102L)))
        .willReturn(Map.of(101L, preview(101L, "요청자"), 102L, preview(102L, "응답자")));

    Page<MatchHistoryView> matchHistories =
        matchHistoryQueryService.findMatchHistories(condition, PAGEABLE);

    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().requesterNickname()).isEqualTo("요청자");
    assertThat(matchHistories.getContent().getFirst().responderNickname()).isEqualTo("응답자");
  }

  @Test
  @DisplayName("전화번호 조건은 전화번호가 일치하는 회원 번호로 바꿔 조회한다.")
  void findMatchHistoriesByPhoneNumber() {
    var condition = new MatchHistorySearchCondition(null, null, "01022222222", null, null);
    given(searchMemberIdsUseCase.searchMemberIds(PHONE_NUMBER_CONDITION)).willReturn(Set.of(102L));
    given(
            adminMatchHistoryQueryRepository.findMatchHistories(
                condition, null, Set.of(102L), PAGEABLE))
        .willReturn(
            pageOf(new RawMatchHistoryView(1L, 101L, 102L, "안녕하세요", null, "WAITING", null)));
    given(findMemberPreviewsUseCase.findMemberPreviews(Set.of(101L, 102L)))
        .willReturn(Map.of(101L, preview(101L, "요청자"), 102L, preview(102L, "응답자")));

    Page<MatchHistoryView> matchHistories =
        matchHistoryQueryService.findMatchHistories(condition, PAGEABLE);

    assertThat(matchHistories.getTotalElements()).isEqualTo(1);
    assertThat(matchHistories.getContent().getFirst().responderNickname()).isEqualTo("응답자");
  }

  @Test
  @DisplayName("닉네임과 전화번호를 함께 주면 각각 따로 회원 번호를 찾아 나머지 조건과 함께 조회한다.")
  void findMatchHistoriesWithAllConditions() {
    var condition =
        new MatchHistorySearchCondition(
            "WAITING", "요청자", "01022222222", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
    given(searchMemberIdsUseCase.searchMemberIds(NICKNAME_CONDITION)).willReturn(Set.of(101L));
    given(searchMemberIdsUseCase.searchMemberIds(PHONE_NUMBER_CONDITION)).willReturn(Set.of(102L));
    given(
            adminMatchHistoryQueryRepository.findMatchHistories(
                condition, Set.of(101L), Set.of(102L), PAGEABLE))
        .willReturn(
            pageOf(new RawMatchHistoryView(1L, 101L, 102L, "안녕하세요", null, "WAITING", null)));
    given(findMemberPreviewsUseCase.findMemberPreviews(Set.of(101L, 102L)))
        .willReturn(Map.of(101L, preview(101L, "요청자"), 102L, preview(102L, "응답자")));

    Page<MatchHistoryView> matchHistories =
        matchHistoryQueryService.findMatchHistories(condition, PAGEABLE);

    assertThat(matchHistories.getContent())
        .containsExactly(new MatchHistoryView(1L, "요청자", "응답자", "안녕하세요", null, "WAITING", null));
  }

  @Test
  @DisplayName("닉네임이 일치하는 회원이 없으면 매치 내역을 조회하지 않고 빈 페이지를 돌려준다.")
  void returnEmptyWhenNoMemberMatchesNickname() {
    var condition = new MatchHistorySearchCondition(null, "요청자", "01022222222", null, null);
    given(searchMemberIdsUseCase.searchMemberIds(NICKNAME_CONDITION)).willReturn(Set.of());

    Page<MatchHistoryView> matchHistories =
        matchHistoryQueryService.findMatchHistories(condition, PAGEABLE);

    assertThat(matchHistories.getTotalElements()).isZero();
    assertThat(matchHistories.getContent()).isEmpty();
    assertThat(matchHistories.getPageable()).isEqualTo(PAGEABLE);
    verify(searchMemberIdsUseCase, never()).searchMemberIds(PHONE_NUMBER_CONDITION);
    verifyNoInteractions(adminMatchHistoryQueryRepository, findMemberPreviewsUseCase);
  }

  @Test
  @DisplayName("전화번호가 일치하는 회원이 없으면 매치 내역을 조회하지 않고 빈 페이지를 돌려준다.")
  void returnEmptyWhenNoMemberMatchesPhoneNumber() {
    var condition = new MatchHistorySearchCondition(null, null, "01022222222", null, null);
    given(searchMemberIdsUseCase.searchMemberIds(PHONE_NUMBER_CONDITION)).willReturn(Set.of());

    Page<MatchHistoryView> matchHistories =
        matchHistoryQueryService.findMatchHistories(condition, PAGEABLE);

    assertThat(matchHistories.getContent()).isEmpty();
    verifyNoInteractions(adminMatchHistoryQueryRepository, findMemberPreviewsUseCase);
  }

  @Test
  @DisplayName("빈 문자열 닉네임·전화번호는 조건으로 쓰지 않는다.")
  void ignoreBlankNicknameAndPhoneNumber() {
    var condition = new MatchHistorySearchCondition(null, " ", "", null, null);
    given(adminMatchHistoryQueryRepository.findMatchHistories(condition, null, null, PAGEABLE))
        .willReturn(new PageImpl<>(List.of(), PAGEABLE, 0));
    given(findMemberPreviewsUseCase.findMemberPreviews(Set.of())).willReturn(Map.of());

    Page<MatchHistoryView> matchHistories =
        matchHistoryQueryService.findMatchHistories(condition, PAGEABLE);

    assertThat(matchHistories.getContent()).isEmpty();
    verifyNoInteractions(searchMemberIdsUseCase);
  }

  @Test
  @DisplayName("회원 정보를 찾지 못한 요청자·응답자는 닉네임을 비워 두고 매치 내역은 남긴다.")
  void keepMatchHistoryWhenMemberIsMissing() {
    var condition = new MatchHistorySearchCondition(null, null, null, null, null);
    given(adminMatchHistoryQueryRepository.findMatchHistories(condition, null, null, PAGEABLE))
        .willReturn(
            pageOf(new RawMatchHistoryView(1L, 101L, 102L, "안녕하세요", null, "WAITING", null)));
    given(findMemberPreviewsUseCase.findMemberPreviews(Set.of(101L, 102L)))
        .willReturn(Map.of(102L, preview(102L, "응답자")));

    Page<MatchHistoryView> matchHistories =
        matchHistoryQueryService.findMatchHistories(condition, PAGEABLE);

    assertThat(matchHistories.getContent())
        .containsExactly(new MatchHistoryView(1L, null, "응답자", "안녕하세요", null, "WAITING", null));
  }

  private Page<RawMatchHistoryView> pageOf(RawMatchHistoryView raw) {
    return new PageImpl<>(List.of(raw), PAGEABLE, 1);
  }

  private MemberPreview preview(long memberId, String nickname) {
    return new MemberPreview(memberId, nickname, null, "SEOUL", 1998, "MALE");
  }
}
