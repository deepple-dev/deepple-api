package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.member.adapter.out.persistence.MemberQueryRepository;
import kr.co.deepple.member.application.port.in.MemberProfileSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindMemberProfilesServiceTest {

  @Mock private MemberQueryRepository memberQueryRepository;

  @InjectMocks private FindMemberProfilesService findMemberProfilesService;

  @Test
  @DisplayName("회원 번호별 프로필을 돌려준다.")
  void findMemberProfiles() {
    MemberProfileSummary first = profile(1L);
    MemberProfileSummary second = profile(2L);
    given(memberQueryRepository.findProfilesByMemberIds(List.of(1L, 2L, 3L)))
        .willReturn(List.of(first, second));

    Map<Long, MemberProfileSummary> profiles =
        findMemberProfilesService.findMemberProfiles(List.of(1L, 2L, 3L));

    assertThat(profiles).containsOnly(Map.entry(1L, first), Map.entry(2L, second));
  }

  @Test
  @DisplayName("회원 번호가 없으면 조회하지 않고 빈 결과를 돌려준다.")
  void returnEmptyWhenNoMemberIds() {
    assertThat(findMemberProfilesService.findMemberProfiles(List.of())).isEmpty();
    verifyNoInteractions(memberQueryRepository);
  }

  private MemberProfileSummary profile(long memberId) {
    return new MemberProfileSummary(
        memberId,
        "닉네임" + memberId,
        null,
        30,
        "MALE",
        "SEOUL",
        "GANGNAM_GU",
        "ENFJ",
        null,
        Set.of());
  }
}
