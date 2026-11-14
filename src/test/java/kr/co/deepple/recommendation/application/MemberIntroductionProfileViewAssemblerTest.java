package kr.co.deepple.recommendation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.member.application.port.in.FindMemberProfilesUseCase;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import kr.co.deepple.recommendation.adapter.out.persistence.IntroductionQueryRepository;
import kr.co.deepple.recommendation.application.dto.MemberIntroductionProfileView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberIntroductionProfileViewAssemblerTest {

  @InjectMocks private MemberIntroductionProfileViewAssembler assembler;

  @Mock private FindMemberProfilesUseCase findMemberProfilesUseCase;

  @Mock private FindLikeLevelUseCase findLikeLevelUseCase;

  @Mock private IntroductionQueryRepository introductionQueryRepository;

  @Test
  @DisplayName("프로필에 좋아요 단계와 소개 여부를 붙여 회원 번호가 큰 순서로 돌려준다.")
  void assemble() {
    long memberId = 1L;
    Set<Long> memberIds = Set.of(2L, 3L, 4L);
    Set<String> hobbies = new LinkedHashSet<>(List.of("CAMPING", "WINE"));
    when(findMemberProfilesUseCase.findMemberProfiles(memberIds))
        .thenReturn(
            Map.of(
                2L,
                profile(2L, 26, hobbies),
                3L,
                profile(3L, 30, Set.of()),
                4L,
                profile(4L, null, Set.of())));
    when(findLikeLevelUseCase.findLikeLevels(memberId, memberIds))
        .thenReturn(Map.of(2L, "INTERESTED"));
    when(introductionQueryRepository.findIntroducedMemberIds(memberId, memberIds))
        .thenReturn(Set.of(3L));

    List<MemberIntroductionProfileView> views = assembler.assemble(memberId, memberIds);

    assertThat(views)
        .containsExactly(
            new MemberIntroductionProfileView(
                4L,
                "image4",
                null,
                "닉네임4",
                "SEOUL",
                "GANGNAM_GU",
                List.of(),
                "ENFP",
                "NONE",
                null,
                false),
            new MemberIntroductionProfileView(
                3L,
                "image3",
                30,
                "닉네임3",
                "SEOUL",
                "GANGNAM_GU",
                List.of(),
                "ENFP",
                "NONE",
                null,
                true),
            new MemberIntroductionProfileView(
                2L,
                "image2",
                26,
                "닉네임2",
                "SEOUL",
                "GANGNAM_GU",
                List.of("CAMPING", "WINE"),
                "ENFP",
                "NONE",
                "INTERESTED",
                false));
  }

  @Test
  @DisplayName("프로필이 없는 회원은 결과에서 빠지고, 좋아요와 소개 여부는 프로필이 있는 회원만 조회한다.")
  void skipMembersWithoutProfile() {
    long memberId = 1L;
    Set<Long> memberIds = Set.of(2L, 3L);
    when(findMemberProfilesUseCase.findMemberProfiles(memberIds))
        .thenReturn(Map.of(3L, profile(3L, 30, Set.of())));
    when(findLikeLevelUseCase.findLikeLevels(memberId, Set.of(3L))).thenReturn(Map.of());
    when(introductionQueryRepository.findIntroducedMemberIds(memberId, Set.of(3L)))
        .thenReturn(Set.of());

    List<MemberIntroductionProfileView> views = assembler.assemble(memberId, memberIds);

    assertThat(views).extracting(MemberIntroductionProfileView::memberId).containsExactly(3L);
  }

  @Test
  @DisplayName("회원 목록이 비어 있으면 조회하지 않고 빈 결과를 돌려준다.")
  void returnEmptyWhenNoMemberIds() {
    assertThat(assembler.assemble(1L, Set.of())).isEmpty();

    verifyNoInteractions(
        findMemberProfilesUseCase, findLikeLevelUseCase, introductionQueryRepository);
  }

  @Test
  @DisplayName("프로필이 하나도 없으면 좋아요와 소개 여부를 조회하지 않고 빈 결과를 돌려준다.")
  void returnEmptyWhenNoProfiles() {
    Set<Long> memberIds = Set.of(2L);
    when(findMemberProfilesUseCase.findMemberProfiles(memberIds)).thenReturn(Map.of());

    assertThat(assembler.assemble(1L, memberIds)).isEmpty();

    verifyNoInteractions(findLikeLevelUseCase, introductionQueryRepository);
  }

  private MemberProfileSummary profile(long memberId, Integer age, Set<String> hobbies) {
    return new MemberProfileSummary(
        memberId,
        "닉네임" + memberId,
        "image" + memberId,
        age,
        "FEMALE",
        "SEOUL",
        "GANGNAM_GU",
        "ENFP",
        "NONE",
        hobbies);
  }
}
