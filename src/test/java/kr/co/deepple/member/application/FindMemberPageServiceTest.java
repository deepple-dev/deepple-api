package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kr.co.deepple.member.application.port.in.MemberPageItem;
import kr.co.deepple.member.application.port.in.MemberPageSearchCondition;
import kr.co.deepple.member.application.port.out.MemberQueryRepository;
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
class FindMemberPageServiceTest {

  @Mock private MemberQueryRepository memberQueryRepository;

  @InjectMocks private FindMemberPageService findMemberPageService;

  @Test
  @DisplayName("검색 조건과 페이지에 맞는 회원 페이지를 돌려준다.")
  void findMemberPage() {
    MemberPageSearchCondition condition =
        new MemberPageSearchCondition(
            "ACTIVE", "MALE", "SILVER", "닉네임", "01012345678", LocalDate.MIN, LocalDate.MAX);
    Pageable pageable = PageRequest.of(0, 10);
    Page<MemberPageItem> page =
        new PageImpl<>(
            List.of(new MemberPageItem(1L, "닉네임", "MALE", "ACTIVE", LocalDateTime.MIN)),
            pageable,
            1);
    given(memberQueryRepository.findPage(condition, pageable)).willReturn(page);

    assertThat(findMemberPageService.findMemberPage(condition, pageable)).isEqualTo(page);
  }
}
