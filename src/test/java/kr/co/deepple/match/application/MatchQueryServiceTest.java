package kr.co.deepple.match.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.match.adapter.out.persistence.MatchQueryRepository;
import kr.co.deepple.match.application.dto.MatchView;
import kr.co.deepple.match.application.dto.MatchViews;
import kr.co.deepple.match.application.dto.RawMatchView;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.MemberPreview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchQueryServiceTest {

  private static final long MEMBER_ID = 1L;

  @Mock private MatchQueryRepository matchQueryRepository;

  @Mock private FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;

  @Mock private FindMemberPreviewsUseCase findMemberPreviewsUseCase;

  @InjectMocks private MatchQueryService matchQueryService;

  @Test
  @DisplayName("차단한 회원 번호를 조회에 넘기고, 상대의 닉네임·사진·지역은 회원 미리보기에서 채운다")
  void attachMemberPreviewToSentMatch() {
    given(findBlockedMemberIdsUseCase.findBlockedMemberIds(MEMBER_ID)).willReturn(Set.of(7L));
    List<RawMatchView> rawMatches = List.of(rawMatch(10L, 200L));
    given(matchQueryRepository.findSentMatches(MEMBER_ID, null, Set.of(7L))).willReturn(rawMatches);
    given(findMemberPreviewsUseCase.findMemberPreviews(List.of(200L)))
        .willReturn(
            Map.of(
                200L,
                new MemberPreview(
                    200L, "상대", "https://example.com/a.jpg", "SEOUL", 1995, "FEMALE")));

    MatchViews result = matchQueryService.getSentMatches(MEMBER_ID, null);

    MatchView match = result.matches().getFirst();
    assertThat(match.matchId()).isEqualTo(10L);
    assertThat(match.opponentId()).isEqualTo(200L);
    assertThat(match.nickName()).isEqualTo("상대");
    assertThat(match.profileImageUrl()).isEqualTo("https://example.com/a.jpg");
    assertThat(match.city()).isEqualTo("SEOUL");
    assertThat(match.opponentMessage()).isEqualTo("상대 메시지");
    assertThat(match.myMessage()).isEqualTo("내 메시지");
    assertThat(result.hasMore()).isFalse();
  }

  @Test
  @DisplayName("13건이 조회되면 12건만 돌려주고 더 있다고 알린다")
  void returnPageAndHasMore() {
    given(findBlockedMemberIdsUseCase.findBlockedMemberIds(MEMBER_ID)).willReturn(Set.of());
    List<RawMatchView> rawMatches =
        IntStream.range(0, 13).mapToObj(i -> rawMatch(100L - i, 200L + i)).toList();
    given(matchQueryRepository.findReceiveMatches(MEMBER_ID, null, Set.of()))
        .willReturn(rawMatches);
    given(findMemberPreviewsUseCase.findMemberPreviews(opponentIds(rawMatches)))
        .willReturn(previews(rawMatches));

    MatchViews result = matchQueryService.getReceivedMatches(MEMBER_ID, null);

    assertThat(result.matches()).hasSize(12);
    assertThat(result.hasMore()).isTrue();
  }

  @Test
  @DisplayName("회원 정보를 찾을 수 없는 상대가 섞여 있으면 다음 메시지를 더 읽어 한 페이지를 채운다")
  void fillPageWhenSomeOpponentsAreMissing() {
    given(findBlockedMemberIdsUseCase.findBlockedMemberIds(MEMBER_ID)).willReturn(Set.of());
    List<RawMatchView> firstBatch =
        IntStream.range(0, 13).mapToObj(i -> rawMatch(100L - i, 200L + i)).toList();
    List<RawMatchView> secondBatch = List.of(rawMatch(87L, 300L), rawMatch(86L, 301L));
    given(matchQueryRepository.findSentMatches(MEMBER_ID, null, Set.of())).willReturn(firstBatch);
    given(matchQueryRepository.findSentMatches(MEMBER_ID, 88L, Set.of())).willReturn(secondBatch);
    Map<Long, MemberPreview> firstPreviews = previews(firstBatch);
    firstPreviews.remove(200L);
    firstPreviews.remove(201L);
    given(findMemberPreviewsUseCase.findMemberPreviews(opponentIds(firstBatch)))
        .willReturn(firstPreviews);
    given(findMemberPreviewsUseCase.findMemberPreviews(opponentIds(secondBatch)))
        .willReturn(previews(secondBatch));

    MatchViews result = matchQueryService.getSentMatches(MEMBER_ID, null);

    assertThat(result.matches()).hasSize(12);
    assertThat(result.matches()).extracting(MatchView::opponentId).doesNotContain(200L, 201L);
    assertThat(result.matches().getLast().matchId()).isEqualTo(87L);
    assertThat(result.hasMore()).isTrue();
  }

  private RawMatchView rawMatch(long matchId, long opponentId) {
    return new RawMatchView(
        matchId, opponentId, "상대 메시지", "내 메시지", "WAITING", LocalDateTime.now(), null);
  }

  private List<Long> opponentIds(List<RawMatchView> rawMatches) {
    return rawMatches.stream().map(RawMatchView::opponentId).toList();
  }

  private Map<Long, MemberPreview> previews(List<RawMatchView> rawMatches) {
    return rawMatches.stream()
        .map(RawMatchView::opponentId)
        .collect(
            Collectors.toMap(
                Function.identity(),
                id -> new MemberPreview(id, "nickname" + id, null, "SEOUL", 1995, "FEMALE")));
  }
}
