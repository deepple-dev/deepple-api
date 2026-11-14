package kr.co.deepple.like.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.like.application.dto.LikeView;
import kr.co.deepple.like.application.dto.LikeViews;
import kr.co.deepple.like.application.dto.RawLikeView;
import kr.co.deepple.like.application.port.out.LikeQueryRepository;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.MemberPreview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeQueryServiceTest {

  private static final int CLIENT_PAGE_SIZE = 12;

  @InjectMocks private LikeQueryService likeQueryService;

  @Mock private LikeQueryRepository likeQueryRepository;

  @Mock private FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;

  @Mock private FindMemberPreviewsUseCase findMemberPreviewsUseCase;

  @BeforeEach
  void setUp() {
    lenient()
        .when(findBlockedMemberIdsUseCase.findBlockedMemberIds(anyLong()))
        .thenReturn(Set.of());
    lenient()
        .when(findMemberPreviewsUseCase.findMemberPreviews(any()))
        .thenAnswer(invocation ->
            invocation.<Collection<Long>>getArgument(0).stream()
                .collect(
                    Collectors.toMap(
                        Function.identity(),
                        id ->
                            new MemberPreview(
                                id,
                                "nickname" + id,
                                "https://example.com/" + id,
                                "SEOUL",
                                1995,
                                "FEMALE"))));
  }

  private List<RawLikeView> createRawLikes(int count) {
    return IntStream.range(0, count)
        .mapToObj(i -> new RawLikeView(i + 1L, i + 100L, i % 2 == 0, LocalDateTime.now()))
        .toList();
  }

  @Nested
  @DisplayName("보낸 좋아요 목록 조회")
  class FindSentLikes {

    @Test
    @DisplayName("데이터가 없는 경우 빈 목록을 반환한다")
    void returnsEmptyListWhenNoData() {
      // given
      long senderId = 1L;
      Long lastLikeId = null;
      given(likeQueryRepository.findSentLikes(senderId, lastLikeId, Set.of()))
          .willReturn(List.of());

      // when
      LikeViews result = likeQueryService.findSentLikes(senderId, lastLikeId);

      // then
      assertThat(result.likes()).isEmpty();
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE보다 적은 데이터가 있는 경우 모든 데이터를 반환한다")
    void returnsAllDataWhenLessThanPageSize() {
      // given
      long senderId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE - 1);
      given(likeQueryRepository.findSentLikes(senderId, lastLikeId, Set.of())).willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findSentLikes(senderId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE - 1);
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE와 같은 수의 데이터가 있는 경우 hasMore는 false이다")
    void returnsExactPageSizeData() {
      // given
      long senderId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE);
      given(likeQueryRepository.findSentLikes(senderId, lastLikeId, Set.of())).willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findSentLikes(senderId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE);
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE보다 많은 데이터가 있는 경우 페이지 크기만큼만 반환한고, hasMore는 true이다")
    void returnsLimitedDataWhenMoreThanPageSize() {
      // given
      long senderId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE + 1);
      given(likeQueryRepository.findSentLikes(senderId, lastLikeId, Set.of())).willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findSentLikes(senderId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE);
      assertThat(result.hasMore()).isTrue();
    }
  }

  @Nested
  @DisplayName("받은 좋아요 목록 조회")
  class FindReceivedLikes {

    @Test
    @DisplayName("데이터가 없는 경우 빈 목록을 반환한다")
    void returnsEmptyListWhenNoData() {
      // given
      long receiverId = 1L;
      Long lastLikeId = null;
      given(likeQueryRepository.findReceivedLikes(receiverId, lastLikeId, Set.of()))
          .willReturn(List.of());

      // when
      LikeViews result = likeQueryService.findReceivedLikes(receiverId, lastLikeId);

      // then
      assertThat(result.likes()).isEmpty();
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE보다 적은 데이터가 있는 경우 모든 데이터를 반환한다")
    void returnsAllDataWhenLessThanPageSize() {
      // given
      long receiverId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE - 1);
      given(likeQueryRepository.findReceivedLikes(receiverId, lastLikeId, Set.of()))
          .willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findReceivedLikes(receiverId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE - 1);
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE와 같은 수의 데이터가 있는 경우 hasMore는 false이다")
    void returnsExactPageSizeData() {
      // given
      long receiverId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE);
      given(likeQueryRepository.findReceivedLikes(receiverId, lastLikeId, Set.of()))
          .willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findReceivedLikes(receiverId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE);
      assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("CLIENT_PAGE_SIZE보다 많은 데이터가 있는 경우 페이지 크기만큼만 반환하고, hasMore는 true이다")
    void returnsLimitedDataWhenMoreThanPageSize() {
      // given
      long receiverId = 1L;
      Long lastLikeId = null;
      List<RawLikeView> rawLikes = createRawLikes(CLIENT_PAGE_SIZE + 1);
      given(likeQueryRepository.findReceivedLikes(receiverId, lastLikeId, Set.of()))
          .willReturn(rawLikes);

      // when
      LikeViews result = likeQueryService.findReceivedLikes(receiverId, lastLikeId);

      // then
      assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE);
      assertThat(result.hasMore()).isTrue();
    }
  }

  @Test
  @DisplayName("상대의 닉네임·사진·지역은 회원 미리보기에서 채우고 나이는 출생연도로 계산한다")
  void attachMemberPreviewToLike() {
    long senderId = 1L;
    given(likeQueryRepository.findSentLikes(senderId, null, Set.of()))
        .willReturn(List.of(new RawLikeView(10L, 200L, true, LocalDateTime.now())));

    LikeViews result = likeQueryService.findSentLikes(senderId, null);

    LikeView like = result.likes().getFirst();
    assertThat(like.opponentId()).isEqualTo(200L);
    assertThat(like.nickname()).isEqualTo("nickname200");
    assertThat(like.profileImageUrl()).isEqualTo("https://example.com/200");
    assertThat(like.city()).isEqualTo("SEOUL");
    assertThat(like.age()).isEqualTo(LocalDate.now().getYear() - 1995);
    assertThat(like.isMutualLike()).isTrue();
  }

  @Test
  @DisplayName("차단한 회원 번호를 조회해 목록 조회에 넘긴다")
  void passBlockedMemberIdsToQuery() {
    long receiverId = 1L;
    given(findBlockedMemberIdsUseCase.findBlockedMemberIds(receiverId)).willReturn(Set.of(7L));
    given(likeQueryRepository.findReceivedLikes(receiverId, null, Set.of(7L)))
        .willReturn(List.of(new RawLikeView(10L, 200L, false, LocalDateTime.now())));

    LikeViews result = likeQueryService.findReceivedLikes(receiverId, null);

    assertThat(result.likes()).hasSize(1);
  }

  @Test
  @DisplayName("회원 정보를 찾을 수 없는 상대의 좋아요는 목록에서 뺀다")
  void skipLikeWhoseOpponentIsMissing() {
    long senderId = 1L;
    given(likeQueryRepository.findSentLikes(senderId, null, Set.of()))
        .willReturn(List.of(new RawLikeView(10L, 200L, true, LocalDateTime.now())));
    doReturn(Map.of()).when(findMemberPreviewsUseCase).findMemberPreviews(any());

    LikeViews result = likeQueryService.findSentLikes(senderId, null);

    assertThat(result.likes()).isEmpty();
  }

  @Test
  @DisplayName("회원 정보를 찾을 수 없는 상대가 섞여 있으면 다음 좋아요를 더 읽어 한 페이지를 채운다")
  void fillPageWhenSomeOpponentsAreMissing() {
    long senderId = 1L;
    List<RawLikeView> firstBatch =
        IntStream.range(0, 13)
            .mapToObj(i -> new RawLikeView(100L - i, 200L + i, false, LocalDateTime.now()))
            .toList();
    List<RawLikeView> secondBatch =
        List.of(
            new RawLikeView(87L, 300L, false, LocalDateTime.now()),
            new RawLikeView(86L, 301L, false, LocalDateTime.now()));
    given(likeQueryRepository.findSentLikes(senderId, null, Set.of())).willReturn(firstBatch);
    given(likeQueryRepository.findSentLikes(senderId, 88L, Set.of())).willReturn(secondBatch);
    doReturn(previewsExcept(firstBatch, 200L, 201L))
        .when(findMemberPreviewsUseCase)
        .findMemberPreviews(firstBatch.stream().map(RawLikeView::opponentId).toList());

    LikeViews result = likeQueryService.findSentLikes(senderId, null);

    assertThat(result.likes()).hasSize(CLIENT_PAGE_SIZE);
    assertThat(result.likes()).extracting(LikeView::opponentId).doesNotContain(200L, 201L);
    assertThat(result.likes().getLast().likeId()).isEqualTo(87L);
    assertThat(result.hasMore()).isTrue();
  }

  @Test
  @DisplayName("출생연도가 없는 상대는 나이를 비워 둔다")
  void leaveAgeEmptyWhenYearOfBirthIsMissing() {
    long senderId = 1L;
    given(likeQueryRepository.findSentLikes(senderId, null, Set.of()))
        .willReturn(List.of(new RawLikeView(10L, 200L, true, LocalDateTime.now())));
    doReturn(Map.of(200L, new MemberPreview(200L, null, null, null, null, null)))
        .when(findMemberPreviewsUseCase)
        .findMemberPreviews(any());

    LikeViews result = likeQueryService.findSentLikes(senderId, null);

    assertThat(result.likes().getFirst().age()).isNull();
  }

  private Map<Long, MemberPreview> previewsExcept(List<RawLikeView> rawLikes, Long... missingIds) {
    Set<Long> missing = Set.of(missingIds);
    return rawLikes.stream()
        .map(RawLikeView::opponentId)
        .filter(id -> !missing.contains(id))
        .collect(
            Collectors.toMap(
                Function.identity(),
                id -> new MemberPreview(id, "nickname" + id, null, "SEOUL", 1995, "FEMALE")));
  }
}
