package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Set;
import kr.co.deepple.member.application.port.in.MemberSearchCondition;
import kr.co.deepple.member.application.port.out.MemberQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchIntroducibleMemberIdsServiceTest {

  @Mock private MemberQueryRepository memberQueryRepository;

  @InjectMocks private SearchIntroducibleMemberIdsService searchIntroducibleMemberIdsService;

  @Test
  @DisplayName("조건과 limit으로 찾은 회원 번호를 돌려준다.")
  void searchMemberIds() {
    MemberSearchCondition condition =
        new MemberSearchCondition(
            Set.of(1L), 20, 30, Set.of(), Set.of("SEOUL"), null, null, null, null, "FEMALE", null);
    given(memberQueryRepository.searchIntroducibleIds(condition, 10L)).willReturn(Set.of(2L, 3L));

    assertThat(searchIntroducibleMemberIdsService.searchMemberIds(condition, 10L))
        .containsExactlyInAnyOrder(2L, 3L);
  }
}
