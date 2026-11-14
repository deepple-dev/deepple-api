package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.adapter.out.persistence.MemberQueryRepository;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.domain.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMemberDetailServiceTest {

  @Mock private MemberQueryRepository memberQueryRepository;

  @InjectMocks private GetMemberDetailService getMemberDetailService;

  @Test
  @DisplayName("회원의 상세 값을 돌려준다.")
  void getMemberDetail() {
    MemberDetail detail =
        new MemberDetail(
            1L,
            "ACTIVE",
            "SILVER",
            false,
            true,
            false,
            "KAKAO",
            "01012345678",
            "kakaoId",
            "닉네임",
            "MALE",
            1997,
            175,
            "OTHERS",
            "DOCTORATE",
            "SEOUL",
            "GANGNAM_GU",
            "ENFJ",
            "NONE",
            "NONE",
            "BUDDHIST",
            Set.of("TRAVEL"),
            null,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            null);
    given(memberQueryRepository.findDetailByMemberId(1L)).willReturn(Optional.of(detail));

    assertThat(getMemberDetailService.getMemberDetail(1L)).isEqualTo(detail);
  }

  @Test
  @DisplayName("회원이 없으면 회원 없음 오류를 던진다.")
  void throwWhenMemberNotFound() {
    given(memberQueryRepository.findDetailByMemberId(1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> getMemberDetailService.getMemberDetail(1L))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
  }
}
