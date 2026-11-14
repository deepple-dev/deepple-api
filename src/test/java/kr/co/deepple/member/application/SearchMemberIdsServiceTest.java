package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.Set;
import kr.co.deepple.member.application.port.in.MemberIdSearchCondition;
import kr.co.deepple.member.application.port.out.MemberQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchMemberIdsServiceTest {

  @Mock private MemberQueryRepository memberQueryRepository;

  @InjectMocks private SearchMemberIdsService searchMemberIdsService;

  @Test
  @DisplayName("검색 조건에 맞는 회원 번호를 돌려준다.")
  void searchMemberIds() {
    MemberIdSearchCondition condition =
        new MemberIdSearchCondition("닉네임", "01012345678", LocalDateTime.MIN, LocalDateTime.MAX);
    given(memberQueryRepository.searchIds(condition)).willReturn(Set.of(1L, 2L));

    assertThat(searchMemberIdsService.searchMemberIds(condition)).containsExactlyInAnyOrder(1L, 2L);
  }

  @Test
  @DisplayName("조건이 하나도 없으면 조회하지 않고 예외를 던진다.")
  void throwWhenConditionIsEmpty() {
    MemberIdSearchCondition condition = new MemberIdSearchCondition(null, null, null, null);

    assertThatThrownBy(() -> searchMemberIdsService.searchMemberIds(condition))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(memberQueryRepository);
  }
}
