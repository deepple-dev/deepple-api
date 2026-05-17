package deepple.deepple.heart.command.application.admingrant;

import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import deepple.deepple.heart.presentation.hearttransaction.AdminHeartGrantRequest;
import deepple.deepple.member.command.application.member.exception.MemberNotFoundException;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class AdminHeartGrantServiceTest {

    @Mock
    private MemberCommandRepository memberCommandRepository;

    @InjectMocks
    private AdminHeartGrantService adminHeartGrantService;

    @Test
    @DisplayName("관리자가 회원에게 하트를 지급하면 회원의 미션 하트 잔액이 증가한다.")
    void grantShouldIncreaseMemberMissionHeartBalance() {
        // given
        long adminId = 99L;
        long memberId = 1L;
        long amount = 50L;
        String reason = "CS 보상";

        Member member = Member.fromPhoneNumber("01012345678");
        setField(member, "id", memberId);
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

        AdminHeartGrantRequest request = new AdminHeartGrantRequest(memberId, amount, reason);

        // when
        adminHeartGrantService.grant(adminId, request);

        // then
        HeartBalance balance = member.getHeartBalance();
        Assertions.assertThat(balance.getMissionHeartBalance()).isEqualTo(amount);
        Assertions.assertThat(balance.getPurchaseHeartBalance()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 멤버에게 지급하려고 하면 MemberNotFoundException 이 발생한다.")
    void grantShouldThrowWhenMemberNotFound() {
        // given
        long adminId = 99L;
        long memberId = 404L;
        AdminHeartGrantRequest request = new AdminHeartGrantRequest(memberId, 10L, "사유");
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

        // when / then
        Assertions.assertThatThrownBy(() -> adminHeartGrantService.grant(adminId, request))
            .isInstanceOf(MemberNotFoundException.class);
    }
}
