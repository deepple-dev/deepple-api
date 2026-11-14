package kr.co.deepple.heart.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.AdminHeartTransactionView;
import kr.co.deepple.heart.application.port.out.AdminHeartTransactionQueryRepository;
import kr.co.deepple.member.application.port.in.FindMemberIdsUseCase;
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
class FindAdminHeartTransactionsServiceTest {

  private static final Pageable PAGEABLE = PageRequest.of(0, 10);

  @Mock private AdminHeartTransactionQueryRepository adminHeartTransactionQueryRepository;

  @Mock private FindMemberIdsUseCase findMemberIdsUseCase;

  @InjectMocks private FindAdminHeartTransactionsService findAdminHeartTransactionsService;

  @Test
  @DisplayName("닉네임이나 전화번호 조건이 있으면 회원 번호를 찾아 그 회원들의 내역만 조회한다.")
  void searchByMemberIdsWhenMemberConditionGiven() {
    AdminHeartTransactionSearchCondition condition =
        new AdminHeartTransactionSearchCondition("딥플", "01012345678", null, null);
    Page<AdminHeartTransactionView> page = new PageImpl<>(List.of(), PAGEABLE, 0);
    when(findMemberIdsUseCase.findMemberIds("딥플", "01012345678")).thenReturn(Set.of(1L, 2L));
    when(adminHeartTransactionQueryRepository.findPage(condition, Set.of(1L, 2L), PAGEABLE))
        .thenReturn(page);

    assertThat(findAdminHeartTransactionsService.findAdminHeartTransactions(condition, PAGEABLE))
        .isSameAs(page);
  }

  @Test
  @DisplayName("닉네임과 전화번호 조건이 모두 없으면 회원을 찾지 않고 전체 내역을 조회한다.")
  void searchAllWhenNoMemberCondition() {
    AdminHeartTransactionSearchCondition condition =
        new AdminHeartTransactionSearchCondition(null, null, null, null);
    Page<AdminHeartTransactionView> page = new PageImpl<>(List.of(), PAGEABLE, 0);
    when(adminHeartTransactionQueryRepository.findPage(condition, null, PAGEABLE)).thenReturn(page);

    assertThat(findAdminHeartTransactionsService.findAdminHeartTransactions(condition, PAGEABLE))
        .isSameAs(page);
    verifyNoInteractions(findMemberIdsUseCase);
  }

  @Test
  @DisplayName("닉네임과 전화번호 중 하나만 있어도 회원 번호를 찾는다.")
  void searchByMemberIdsWhenOnlyNicknameGiven() {
    AdminHeartTransactionSearchCondition condition =
        new AdminHeartTransactionSearchCondition("딥플", null, null, null);
    Page<AdminHeartTransactionView> page = new PageImpl<>(List.of(), PAGEABLE, 0);
    when(findMemberIdsUseCase.findMemberIds("딥플", null)).thenReturn(Set.of(1L));
    when(adminHeartTransactionQueryRepository.findPage(condition, Set.of(1L), PAGEABLE))
        .thenReturn(page);

    assertThat(findAdminHeartTransactionsService.findAdminHeartTransactions(condition, PAGEABLE))
        .isSameAs(page);
  }
}
