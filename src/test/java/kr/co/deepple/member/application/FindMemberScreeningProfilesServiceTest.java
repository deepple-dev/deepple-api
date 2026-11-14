package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import kr.co.deepple.member.application.dto.ProfileImageView;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.member.application.port.in.MemberScreeningProfile;
import kr.co.deepple.member.application.port.out.MemberQueryRepository;
import kr.co.deepple.member.application.port.out.ProfileImageQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindMemberScreeningProfilesServiceTest {

  @Mock private MemberQueryRepository memberQueryRepository;
  @Mock private ProfileImageQueryRepository profileImageQueryRepository;

  @InjectMocks private FindMemberScreeningProfilesService findMemberScreeningProfilesService;

  @Test
  @DisplayName("회원 번호별 심사용 프로필을 돌려준다.")
  void findMemberScreeningProfiles() {
    MemberScreeningProfile first = profile(1L);
    MemberScreeningProfile second = profile(2L);
    given(memberQueryRepository.findScreeningProfilesByMemberIds(List.of(1L, 2L, 3L)))
        .willReturn(List.of(first, second));

    Map<Long, MemberScreeningProfile> profiles =
        findMemberScreeningProfilesService.findMemberScreeningProfiles(List.of(1L, 2L, 3L));

    assertThat(profiles).containsOnly(Map.entry(1L, first), Map.entry(2L, second));
  }

  @Test
  @DisplayName("회원 번호가 없으면 조회하지 않고 빈 결과를 돌려준다.")
  void returnEmptyWhenNoMemberIds() {
    assertThat(findMemberScreeningProfilesService.findMemberScreeningProfiles(List.of())).isEmpty();
    verifyNoInteractions(memberQueryRepository);
  }

  @Test
  @DisplayName("회원의 프로필 사진을 조회한 순서대로 돌려준다.")
  void findProfileImages() {
    given(profileImageQueryRepository.findByMemberId(1L))
        .willReturn(
            List.of(
                new ProfileImageView(10L, "https://example.com/1", true, 1),
                new ProfileImageView(11L, "https://example.com/2", false, 2)));

    assertThat(findMemberScreeningProfilesService.findProfileImages(1L))
        .containsExactly(
            new MemberProfileImage("https://example.com/1", 1, true),
            new MemberProfileImage("https://example.com/2", 2, false));
  }

  private MemberScreeningProfile profile(long memberId) {
    return new MemberScreeningProfile(
        memberId, "닉네임" + memberId, 1998, "MALE", LocalDateTime.of(2026, 1, 1, 0, 0));
  }
}
