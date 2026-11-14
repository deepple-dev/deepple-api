package kr.co.deepple.community.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;
import kr.co.deepple.block.application.port.in.FindBlockedMemberIdsUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.application.dto.AdminSelfIntroductionSearchCondition;
import kr.co.deepple.community.application.dto.AdminSelfIntroductionView;
import kr.co.deepple.community.application.dto.RawAdminSelfIntroductionView;
import kr.co.deepple.community.application.dto.RawSelfIntroductionView;
import kr.co.deepple.community.application.dto.SelfIntroductionSearchRequest;
import kr.co.deepple.community.application.dto.SelfIntroductionSummaryView;
import kr.co.deepple.community.application.dto.SelfIntroductionView;
import kr.co.deepple.community.application.port.out.AdminSelfIntroductionQueryRepository;
import kr.co.deepple.community.application.port.out.SelfIntroductionQueryRepository;
import kr.co.deepple.community.domain.CommunityErrorCode;
import kr.co.deepple.datingexam.application.port.in.FindDominantPersonalityTypesUseCase;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.member.application.port.in.FindMemberIdsUseCase;
import kr.co.deepple.member.application.port.in.FindMemberPreviewsUseCase;
import kr.co.deepple.member.application.port.in.FindMemberProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberPreview;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SelfIntroductionQueryServiceTest {

  private static final long MEMBER_ID = 1L;
  private static final int PAGE_SIZE = 10;
  private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 2, 3, 4);

  @InjectMocks private SelfIntroductionQueryService selfIntroductionQueryService;

  @Mock private SelfIntroductionQueryRepository selfIntroductionQueryRepository;
  @Mock private AdminSelfIntroductionQueryRepository adminSelfIntroductionQueryRepository;
  @Mock private FindBlockedMemberIdsUseCase findBlockedMemberIdsUseCase;
  @Mock private FindMemberPreviewsUseCase findMemberPreviewsUseCase;
  @Mock private FindMemberProfilesUseCase findMemberProfilesUseCase;
  @Mock private FindMemberIdsUseCase findMemberIdsUseCase;
  @Mock private FindDominantPersonalityTypesUseCase findDominantPersonalityTypesUseCase;
  @Mock private FindLikeLevelUseCase findLikeLevelUseCase;

  @Captor private ArgumentCaptor<Collection<Long>> memberIdsCaptor;

  private final Map<Long, MemberPreview> members = new HashMap<>();

  @BeforeEach
  void setUp() {
    lenient()
        .when(findBlockedMemberIdsUseCase.findBlockedMemberIds(anyLong()))
        .thenReturn(Set.of());
    lenient()
        .when(findMemberPreviewsUseCase.findMemberPreviews(any()))
        .thenAnswer(invocation -> {
          Map<Long, MemberPreview> found = new HashMap<>();
          for (Long id : invocation.<Collection<Long>>getArgument(0)) {
            if (members.containsKey(id)) {
              found.put(id, members.get(id));
            }
          }
          return found;
        });
    lenient()
        .when(findDominantPersonalityTypesUseCase.findDominantPersonalityTypes(any()))
        .thenReturn(Map.of());
  }

  private void member(long id, String gender, Integer age, String city) {
    members.put(
        id,
        new MemberPreview(
            id,
            "nickname" + id,
            null,
            city,
            age == null ? null : Year.now().getValue() - age + 1,
            gender));
  }

  private RawSelfIntroductionView raw(long id, long memberId) {
    return new RawSelfIntroductionView(
        id, memberId, "제목" + id, "내용" + id, "image" + id, CREATED_AT);
  }

  private SelfIntroductionSearchRequest request(
      List<String> cities, Integer fromAge, Integer toAge, String gender) {
    return new SelfIntroductionSearchRequest(cities, fromAge, toAge, gender, null);
  }

  private List<Long> idsOf(List<SelfIntroductionSummaryView> views) {
    return views.stream().map(SelfIntroductionSummaryView::id).toList();
  }

  @Nested
  @DisplayName("셀프 소개 목록 조회")
  class FindSelfIntroductions {

    @BeforeEach
    void setUp() {
      member(10L, "MALE", 25, "SEOUL");
      member(20L, "FEMALE", 35, "DAEJEON");
    }

    private void givenRaws(RawSelfIntroductionView... raws) {
      given(selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, null, Set.of(), 50))
          .willReturn(List.of(raws));
    }

    private List<Long> find(SelfIntroductionSearchRequest request) {
      return idsOf(
          selfIntroductionQueryService
              .findSelfIntroductions(MEMBER_ID, request)
              .selfIntroductions());
    }

    @Test
    @DisplayName("검색 조건이 없으면 조회된 글을 순서대로 작성자 정보와 함께 돌려준다.")
    void returnsAllWithAuthor() {
      // given
      givenRaws(raw(4L, 10L), raw(3L, 20L));
      given(findDominantPersonalityTypesUseCase.findDominantPersonalityTypes(Set.of(10L, 20L)))
          .willReturn(Map.of(10L, "STIMULATING_ADVENTURER"));

      // when
      List<SelfIntroductionSummaryView> views =
          selfIntroductionQueryService
              .findSelfIntroductions(MEMBER_ID, request(null, null, null, null))
              .selfIntroductions();

      // then
      assertThat(views)
          .containsExactly(
              new SelfIntroductionSummaryView(
                  4L,
                  "nickname10",
                  members.get(10L).yearOfBirth(),
                  "제목4",
                  "내용4",
                  "image4",
                  CREATED_AT,
                  "STIMULATING_ADVENTURER"),
              new SelfIntroductionSummaryView(
                  3L,
                  "nickname20",
                  members.get(20L).yearOfBirth(),
                  "제목3",
                  "내용3",
                  "image3",
                  CREATED_AT,
                  null));
    }

    @Test
    @DisplayName("성별을 명시하면 그 성별의 회원이 쓴 글만 돌려주고, 대소문자는 구분하지 않는다.")
    void filtersByGender() {
      givenRaws(raw(4L, 10L), raw(3L, 20L), raw(2L, 10L));

      assertThat(find(request(null, null, null, "MALE"))).containsExactly(4L, 2L);
      assertThat(find(request(null, null, null, "female"))).containsExactly(3L);
    }

    @Test
    @DisplayName("최소 나이를 명시하면 그 나이 이상인 회원이 쓴 글만 돌려준다.")
    void filtersByFromAge() {
      givenRaws(raw(4L, 10L), raw(3L, 20L));

      assertThat(find(request(null, 25, null, null))).containsExactly(4L, 3L);
      assertThat(find(request(null, 26, null, null))).containsExactly(3L);
      assertThat(find(request(null, 36, null, null))).isEmpty();
    }

    @Test
    @DisplayName("최대 나이를 명시하면 그 나이 이하인 회원이 쓴 글만 돌려준다.")
    void filtersByToAge() {
      givenRaws(raw(4L, 10L), raw(3L, 20L));

      assertThat(find(request(null, null, 35, null))).containsExactly(4L, 3L);
      assertThat(find(request(null, null, 34, null))).containsExactly(4L);
      assertThat(find(request(null, null, 24, null))).isEmpty();
    }

    @Test
    @DisplayName("최소·최대 나이를 명시하면 양 끝을 포함해 그 사이인 회원이 쓴 글만 돌려준다.")
    void filtersByAgeRange() {
      givenRaws(raw(4L, 10L), raw(3L, 20L));

      assertThat(find(request(null, 25, 35, null))).containsExactly(4L, 3L);
      assertThat(find(request(null, 26, 35, null))).containsExactly(3L);
      assertThat(find(request(null, 25, 34, null))).containsExactly(4L);
      assertThat(find(request(null, 26, 34, null))).isEmpty();
    }

    @Test
    @DisplayName("나이 조건이 있으면 출생연도가 없는 회원의 글은 빼고, 조건이 없으면 포함한다.")
    void handlesMemberWithoutYearOfBirth() {
      member(30L, "MALE", null, "SEOUL");
      givenRaws(raw(4L, 30L));

      assertThat(find(request(null, 20, null, null))).isEmpty();
      assertThat(find(request(null, null, 40, null))).isEmpty();
      assertThat(find(request(null, null, null, null))).containsExactly(4L);
    }

    @Test
    @DisplayName("지역을 명시하면 그 지역 중 한 곳에 사는 회원이 쓴 글만 돌려준다.")
    void filtersByPreferredCities() {
      member(30L, "MALE", 25, null);
      givenRaws(raw(4L, 10L), raw(3L, 20L), raw(2L, 30L));

      assertThat(find(request(List.of("SEOUL"), null, null, null))).containsExactly(4L);
      assertThat(find(request(List.of("DAEJEON", "SEOUL"), null, null, null)))
          .containsExactly(4L, 3L);
      assertThat(find(request(List.of("BUSAN"), null, null, null))).isEmpty();
      assertThat(find(request(List.of(), null, null, null))).containsExactly(4L, 3L, 2L);
    }

    @Test
    @DisplayName("모든 조건을 명시하면 전부 만족하는 회원이 쓴 글만 돌려준다.")
    void filtersByAllConditions() {
      member(30L, "MALE", 25, "BUSAN");
      member(40L, "MALE", 40, "SEOUL");
      givenRaws(raw(5L, 10L), raw(4L, 20L), raw(3L, 30L), raw(2L, 40L));

      assertThat(find(request(List.of("DAEJEON", "SEOUL"), 25, 35, "MALE"))).containsExactly(5L);
    }

    @Test
    @DisplayName("내가 차단한 회원 번호와 lastId를 조회에 넘긴다.")
    void passesBlockedIdsAndCursor() {
      // given
      given(findBlockedMemberIdsUseCase.findBlockedMemberIds(MEMBER_ID)).willReturn(Set.of(20L));
      given(selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, 7L, Set.of(20L), 50))
          .willReturn(List.of(raw(6L, 10L)));

      // when
      List<SelfIntroductionSummaryView> views =
          selfIntroductionQueryService
              .findSelfIntroductions(
                  MEMBER_ID, new SelfIntroductionSearchRequest(null, null, null, null, 7L))
              .selfIntroductions();

      // then
      assertThat(idsOf(views)).containsExactly(6L);
    }

    @Test
    @DisplayName("회원 정보를 찾을 수 없는 작성자의 글은 뺀다.")
    void excludesWhenAuthorMissing() {
      givenRaws(raw(4L, 999L), raw(3L, 10L));

      assertThat(find(request(null, null, null, null))).containsExactly(3L);
    }

    @Test
    @DisplayName("조건에 맞는 글이 10건을 넘으면 10건만 돌려주고 더 읽지 않는다.")
    void returnsOnlyPageSize() {
      // given
      given(selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, null, Set.of(), 50))
          .willReturn(
              LongStream.rangeClosed(1, 50).map(i -> 51 - i).mapToObj(id -> raw(id, 10L)).toList());

      // when
      List<Long> ids = find(request(null, null, null, null));

      // then
      assertThat(ids)
          .containsExactlyElementsOf(
              LongStream.rangeClosed(41, 50).map(i -> 91 - i).boxed().toList());
      verify(selfIntroductionQueryRepository, times(1))
          .findSelfIntroductions(anyLong(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("10건이 차지 않으면 마지막으로 본 글 다음부터 읽는 건수를 2배씩 늘려 더 읽고, 성격 유형은 최종 작성자만 조회한다.")
    void fetchesMoreUntilPageIsFilled() {
      // given: 300~251 여성, 250~151 여성, 150~ 남성
      given(selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, null, Set.of(), 50))
          .willReturn(descending(300, 251, 20L));
      given(selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, 251L, Set.of(), 100))
          .willReturn(descending(250, 151, 20L));
      given(selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, 151L, Set.of(), 200))
          .willReturn(descending(150, 1, 10L));

      // when
      List<Long> ids = find(request(null, null, null, "MALE"));

      // then
      assertThat(ids).containsExactly(150L, 149L, 148L, 147L, 146L, 145L, 144L, 143L, 142L, 141L);
      verify(findDominantPersonalityTypesUseCase, times(1))
          .findDominantPersonalityTypes(memberIdsCaptor.capture());
      assertThat(memberIdsCaptor.getValue()).containsExactly(10L);
    }

    @Test
    @DisplayName("한 번에 읽는 건수는 1,000건을 넘지 않고, 횟수 제한 없이 끝까지 읽는다.")
    void capsFetchSize() {
      // given
      int[] calls = {0};
      given(
              selfIntroductionQueryRepository.findSelfIntroductions(
                  anyLong(), any(), any(), anyInt()))
          .willAnswer(invocation -> {
            if (++calls[0] == 8) {
              return List.of();
            }
            Long cursor = invocation.getArgument(1);
            int limit = invocation.getArgument(3);
            long from = cursor == null ? 100_000L : cursor - 1;
            return descending(from, from - limit + 1, 20L);
          });

      // when
      List<Long> ids = find(request(null, null, null, "MALE"));

      // then
      ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
      assertThat(ids).isEmpty();
      verify(selfIntroductionQueryRepository, times(8))
          .findSelfIntroductions(anyLong(), any(), any(), limitCaptor.capture());
      assertThat(limitCaptor.getAllValues())
          .containsExactly(50, 100, 200, 400, 800, 1_000, 1_000, 1_000);
    }

    @Test
    @DisplayName("읽어 온 건수가 요청한 건수보다 적으면 10건이 차지 않아도 더 읽지 않는다.")
    void stopsWhenNoMoreRows() {
      // given
      given(selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, null, Set.of(), 50))
          .willReturn(descending(300, 251, 20L));
      given(selfIntroductionQueryRepository.findSelfIntroductions(MEMBER_ID, 251L, Set.of(), 100))
          .willReturn(List.of(raw(250L, 10L), raw(249L, 20L)));

      // when
      List<Long> ids = find(request(null, null, null, "MALE"));

      // then
      assertThat(ids).containsExactly(250L);
      verify(selfIntroductionQueryRepository, times(2))
          .findSelfIntroductions(anyLong(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("글이 없으면 빈 목록을 돌려준다.")
    void returnsEmpty() {
      givenRaws();

      assertThat(find(request(null, null, null, null))).isEmpty();
    }

    private List<RawSelfIntroductionView> descending(long from, long to, long memberId) {
      return LongStream.rangeClosed(to, from)
          .map(i -> from + to - i)
          .mapToObj(id -> raw(id, memberId))
          .toList();
    }
  }

  @Nested
  @DisplayName("자신의 셀프 소개 목록 조회")
  class FindMySelfIntroductions {

    @Test
    @DisplayName("자신의 글에 자신의 닉네임·출생연도·성격 유형을 붙여 돌려준다.")
    void returnsMine() {
      // given
      member(MEMBER_ID, "MALE", 25, "SEOUL");
      given(selfIntroductionQueryRepository.findMySelfIntroductions(MEMBER_ID, 9L, PAGE_SIZE))
          .willReturn(List.of(raw(8L, MEMBER_ID), raw(7L, MEMBER_ID)));
      given(findDominantPersonalityTypesUseCase.findDominantPersonalityTypes(Set.of(MEMBER_ID)))
          .willReturn(Map.of(MEMBER_ID, "RATIONAL_REALIST"));

      // when
      List<SelfIntroductionSummaryView> views =
          selfIntroductionQueryService.findMySelfIntroductions(MEMBER_ID, 9L).selfIntroductions();

      // then
      assertThat(idsOf(views)).containsExactly(8L, 7L);
      assertThat(views)
          .allSatisfy(view -> {
            assertThat(view.nickname()).isEqualTo("nickname" + MEMBER_ID);
            assertThat(view.yearOfBirth()).isEqualTo(members.get(MEMBER_ID).yearOfBirth());
            assertThat(view.personalityType()).isEqualTo("RATIONAL_REALIST");
          });
      verifyNoInteractions(findBlockedMemberIdsUseCase);
    }

    @Test
    @DisplayName("회원 정보를 찾을 수 없으면 빈 목록을 돌려준다.")
    void returnsEmptyWhenMemberMissing() {
      given(selfIntroductionQueryRepository.findMySelfIntroductions(MEMBER_ID, null, PAGE_SIZE))
          .willReturn(List.of(raw(8L, MEMBER_ID)));

      assertThat(
              selfIntroductionQueryService
                  .findMySelfIntroductions(MEMBER_ID, null)
                  .selfIntroductions())
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("셀프 소개 상세 조회")
  class GetSelfIntroduction {

    private static final long ID = 5L;
    private static final long AUTHOR_ID = 10L;

    private final MemberProfileSummary author =
        new MemberProfileSummary(
            AUTHOR_ID,
            "닉네임",
            "https://example.com/a.jpg",
            25,
            "FEMALE",
            "SEOUL",
            "GANGBUK_GU",
            "ENFJ",
            "NONE",
            Set.of("ANIMATION", "BOARD_GAMES"));

    @Test
    @DisplayName("글에 작성자 프로필·성격 유형·내가 보낸 좋아요·프로필 교환 상태를 붙여 돌려준다.")
    void returnsDetail() {
      // given
      given(selfIntroductionQueryRepository.findSelfIntroduction(ID))
          .willReturn(Optional.of(raw(ID, AUTHOR_ID)));
      given(findMemberProfilesUseCase.findMemberProfiles(Set.of(AUTHOR_ID)))
          .willReturn(Map.of(AUTHOR_ID, author));
      given(findDominantPersonalityTypesUseCase.findDominantPersonalityTypes(Set.of(AUTHOR_ID)))
          .willReturn(Map.of(AUTHOR_ID, "REALISTIC_SHELTER"));
      given(findLikeLevelUseCase.findLikeLevel(MEMBER_ID, AUTHOR_ID))
          .willReturn(Optional.of("HIGHLY_INTERESTED"));
      given(selfIntroductionQueryRepository.findProfileExchangeStatus(MEMBER_ID, AUTHOR_ID))
          .willReturn(Optional.of("APPROVE"));

      // when
      SelfIntroductionView view = selfIntroductionQueryService.getSelfIntroduction(ID, MEMBER_ID);

      // then
      assertThat(view.title()).isEqualTo("제목" + ID);
      assertThat(view.content()).isEqualTo("내용" + ID);
      assertThat(view.imageUrl()).isEqualTo("image" + ID);
      assertThat(view.createdAt()).isEqualTo(CREATED_AT);
      assertThat(view.like()).isEqualTo("HIGHLY_INTERESTED");
      assertThat(view.profileExchangeStatus()).isEqualTo("APPROVE");
      assertThat(view.memberBasicInfo().memberId()).isEqualTo(AUTHOR_ID);
      assertThat(view.memberBasicInfo().nickname()).isEqualTo("닉네임");
      assertThat(view.memberBasicInfo().age()).isEqualTo(25);
      assertThat(view.memberBasicInfo().city()).isEqualTo("SEOUL");
      assertThat(view.memberBasicInfo().district()).isEqualTo("GANGBUK_GU");
      assertThat(view.memberBasicInfo().mbti()).isEqualTo("ENFJ");
      assertThat(view.memberBasicInfo().hobbies())
          .containsExactlyInAnyOrder("ANIMATION", "BOARD_GAMES");
      assertThat(view.memberBasicInfo().gender()).isEqualTo("FEMALE");
      assertThat(view.memberBasicInfo().personalityType()).isEqualTo("REALISTIC_SHELTER");
    }

    @Test
    @DisplayName("성격 유형·좋아요·프로필 교환이 없으면 그 값은 null이다.")
    void returnsNullsWhenAbsent() {
      // given
      given(selfIntroductionQueryRepository.findSelfIntroduction(ID))
          .willReturn(Optional.of(raw(ID, AUTHOR_ID)));
      given(findMemberProfilesUseCase.findMemberProfiles(Set.of(AUTHOR_ID)))
          .willReturn(Map.of(AUTHOR_ID, author));
      given(findLikeLevelUseCase.findLikeLevel(MEMBER_ID, AUTHOR_ID)).willReturn(Optional.empty());
      given(selfIntroductionQueryRepository.findProfileExchangeStatus(MEMBER_ID, AUTHOR_ID))
          .willReturn(Optional.empty());

      // when
      SelfIntroductionView view = selfIntroductionQueryService.getSelfIntroduction(ID, MEMBER_ID);

      // then
      assertThat(view.like()).isNull();
      assertThat(view.profileExchangeStatus()).isNull();
      assertThat(view.memberBasicInfo().personalityType()).isNull();
    }

    @Test
    @DisplayName("글이 없으면 SELF_INTRODUCTION_NOT_FOUND 예외가 발생한다.")
    void throwsWhenNotFound() {
      given(selfIntroductionQueryRepository.findSelfIntroduction(ID)).willReturn(Optional.empty());

      assertThatThrownBy(() -> selfIntroductionQueryService.getSelfIntroduction(ID, MEMBER_ID))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND);
      verifyNoInteractions(findMemberProfilesUseCase);
    }

    @Test
    @DisplayName("작성자 프로필을 찾을 수 없으면 SELF_INTRODUCTION_NOT_FOUND 예외가 발생한다.")
    void throwsWhenAuthorMissing() {
      given(selfIntroductionQueryRepository.findSelfIntroduction(ID))
          .willReturn(Optional.of(raw(ID, AUTHOR_ID)));
      given(findMemberProfilesUseCase.findMemberProfiles(Set.of(AUTHOR_ID))).willReturn(Map.of());

      assertThatThrownBy(() -> selfIntroductionQueryService.getSelfIntroduction(ID, MEMBER_ID))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("관리자 셀프 소개 목록 조회")
  class FindAdminSelfIntroductions {

    private static final Pageable PAGEABLE = PageRequest.of(0, 5);

    private RawAdminSelfIntroductionView rawAdmin(long id, long memberId) {
      return new RawAdminSelfIntroductionView(
          id,
          memberId,
          true,
          "내용" + id,
          "image" + id,
          LocalDateTime.of(2026, 1, 2, 3, 4),
          LocalDateTime.of(2026, 2, 3, 4, 5),
          null);
    }

    @Test
    @DisplayName("닉네임·전화번호 조건이 없으면 회원을 찾지 않고 조회해 닉네임·성별을 붙인다.")
    void returnsAllWithAuthor() {
      // given
      member(10L, "MALE", 25, "SEOUL");
      AdminSelfIntroductionSearchCondition condition =
          new AdminSelfIntroductionSearchCondition(true, null, null, null, null);
      given(adminSelfIntroductionQueryRepository.findSelfIntroductions(condition, null, PAGEABLE))
          .willReturn(new PageImpl<>(List.of(rawAdmin(3L, 10L)), PAGEABLE, 7));

      // when
      Page<AdminSelfIntroductionView> page =
          selfIntroductionQueryService.findAdminSelfIntroductions(condition, PAGEABLE);

      // then
      assertThat(page.getTotalElements()).isEqualTo(7);
      assertThat(page.getContent())
          .containsExactly(
              new AdminSelfIntroductionView(
                  3L,
                  "nickname10",
                  "MALE",
                  true,
                  "내용3",
                  "image3",
                  "2026/01/02",
                  "2026/02/03",
                  null));
      verifyNoInteractions(findMemberIdsUseCase);
    }

    @Test
    @DisplayName("닉네임이나 전화번호 조건이 있으면 회원 번호를 찾아 그 회원들의 글만 조회한다.")
    void searchesByMemberIds() {
      // given
      AdminSelfIntroductionSearchCondition condition =
          new AdminSelfIntroductionSearchCondition(null, "딥플", "01012345678", null, null);
      given(findMemberIdsUseCase.findMemberIds("딥플", "01012345678")).willReturn(Set.of(10L));
      given(
              adminSelfIntroductionQueryRepository.findSelfIntroductions(
                  condition, Set.of(10L), PAGEABLE))
          .willReturn(new PageImpl<>(List.of(rawAdmin(3L, 10L)), PAGEABLE, 1));

      // when
      Page<AdminSelfIntroductionView> page =
          selfIntroductionQueryService.findAdminSelfIntroductions(condition, PAGEABLE);

      // then
      assertThat(page.getContent())
          .extracting(AdminSelfIntroductionView::selfIntroductionId)
          .containsExactly(3L);
    }

    @Test
    @DisplayName("빈 문자열 조건은 없는 것으로 다룬다.")
    void treatsBlankAsNull() {
      // given
      AdminSelfIntroductionSearchCondition onlyPhone =
          new AdminSelfIntroductionSearchCondition(null, " ", "01012345678", null, null);
      AdminSelfIntroductionSearchCondition allBlank =
          new AdminSelfIntroductionSearchCondition(null, "", " ", null, null);
      given(findMemberIdsUseCase.findMemberIds(null, "01012345678")).willReturn(Set.of(10L));
      given(
              adminSelfIntroductionQueryRepository.findSelfIntroductions(
                  onlyPhone, Set.of(10L), PAGEABLE))
          .willReturn(new PageImpl<>(List.of(), PAGEABLE, 0));
      given(adminSelfIntroductionQueryRepository.findSelfIntroductions(allBlank, null, PAGEABLE))
          .willReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

      // when
      selfIntroductionQueryService.findAdminSelfIntroductions(onlyPhone, PAGEABLE);
      selfIntroductionQueryService.findAdminSelfIntroductions(allBlank, PAGEABLE);

      // then
      verify(findMemberIdsUseCase, times(1)).findMemberIds(any(), any());
      verify(adminSelfIntroductionQueryRepository)
          .findSelfIntroductions(eq(allBlank), isNull(), eq(PAGEABLE));
    }

    @Test
    @DisplayName("조건에 맞는 회원이 없으면 글을 조회하지 않고 빈 페이지를 돌려준다.")
    void returnsEmptyPageWhenNoMember() {
      // given
      AdminSelfIntroductionSearchCondition condition =
          new AdminSelfIntroductionSearchCondition(null, "없는닉네임", null, null, null);
      given(findMemberIdsUseCase.findMemberIds("없는닉네임", null)).willReturn(Set.of());

      // when
      Page<AdminSelfIntroductionView> page =
          selfIntroductionQueryService.findAdminSelfIntroductions(condition, PAGEABLE);

      // then
      assertThat(page.getContent()).isEmpty();
      assertThat(page.getTotalElements()).isZero();
      verify(adminSelfIntroductionQueryRepository, never())
          .findSelfIntroductions(any(), any(), any());
    }

    @Test
    @DisplayName("회원 정보를 찾을 수 없는 작성자의 글은 닉네임·성별을 비워 돌려준다.")
    void keepsRowWhenAuthorMissing() {
      // given
      AdminSelfIntroductionSearchCondition condition =
          new AdminSelfIntroductionSearchCondition(null, null, null, null, null);
      given(adminSelfIntroductionQueryRepository.findSelfIntroductions(condition, null, PAGEABLE))
          .willReturn(new PageImpl<>(List.of(rawAdmin(3L, 999L)), PAGEABLE, 1));

      // when
      Page<AdminSelfIntroductionView> page =
          selfIntroductionQueryService.findAdminSelfIntroductions(condition, PAGEABLE);

      // then
      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().getFirst().selfIntroductionId()).isEqualTo(3L);
      assertThat(page.getContent().getFirst().nickname()).isNull();
      assertThat(page.getContent().getFirst().gender()).isNull();
    }
  }
}
