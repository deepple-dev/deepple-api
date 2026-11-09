package deepple.deepple.member.command.application.member;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberHeartBalanceServiceTest {

    @Mock
    private MemberCommandRepository memberCommandRepository;

    @InjectMocks
    private MemberHeartBalanceService memberHeartBalanceService;

    @Nested
    @DisplayName("구매한 하트를 지급할 때")
    class GrantPurchasedHeartsTests {
        @Test
        @DisplayName("멤버가 존재하지 않으면 예외를 던집니다.")
        void shouldThrowExceptionWhenMemberNotFound() {
            // given
            Long memberId = 1L;
            when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberHeartBalanceService.grantPurchasedHearts(memberId, 100L))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("멤버가 존재하면 구매한 하트를 지급합니다.")
        void shouldGrantPurchasedHeartsWhenMemberExists() {
            // given
            Long memberId = 1L;
            Member member = mock(Member.class);
            when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.of(member));
            Long amount = 100L;

            // when
            memberHeartBalanceService.grantPurchasedHearts(memberId, amount);

            // then
            verify(member).gainPurchaseHeart(amount);
        }
    }

    @Nested
    @DisplayName("미션 하트를 지급할 때")
    class GrantMissionHeartsTests {
        @Test
        @DisplayName("멤버가 존재하지 않으면 예외를 던집니다.")
        void shouldThrowExceptionWhenMemberNotFound() {
            // given
            Long memberId = 1L;
            when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberHeartBalanceService.grantMissionHearts(memberId, 50L, "Daily Login"))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("멤버가 존재하면 미션 하트를 지급합니다.")
        void shouldGrantMissionHeartsWhenMemberExists() {
            // given
            Long memberId = 1L;
            Member member = mock(Member.class);
            when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.of(member));
            Long amount = 50L;
            String actionType = "Daily Login";

            // when
            memberHeartBalanceService.grantMissionHearts(memberId, amount, actionType);

            // then
            verify(member).gainMissionHeart(amount, actionType);
        }
    }

    @Nested
    @DisplayName("구매한 하트를 환불할 때")
    class RefundPurchasedHeartsTests {
        @Test
        @DisplayName("멤버가 존재하지 않으면 예외를 던집니다.")
        void shouldThrowExceptionWhenMemberNotFound() {
            // given
            Long memberId = 1L;
            when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberHeartBalanceService.refundPurchasedHearts(memberId, 100L))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("멤버가 존재하면 구매한 하트를 환불합니다.")
        void shouldRefundPurchasedHeartsWhenMemberExists() {
            // given
            Long memberId = 1L;
            Member member = mock(Member.class);
            when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.of(member));
            Long amount = 100L;

            // when
            memberHeartBalanceService.refundPurchasedHearts(memberId, amount);

            // then
            verify(member).refundPurchaseHeart(amount);
        }

        @Test
        @DisplayName("환불 금액이 0보다 큰 경우 정상적으로 처리됩니다.")
        void shouldRefundPurchasedHeartsWithPositiveAmount() {
            // given
            Long memberId = 1L;
            Member member = mock(Member.class);
            when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.of(member));
            Long amount = 50L;

            // when
            memberHeartBalanceService.refundPurchasedHearts(memberId, amount);

            // then
            verify(memberCommandRepository).findByIdForUpdate(memberId);
            verify(member).refundPurchaseHeart(amount);
        }

        @Test
        @DisplayName("큰 금액의 하트를 환불할 때 정상적으로 처리됩니다.")
        void shouldRefundLargeAmountOfPurchasedHearts() {
            // given
            Long memberId = 1L;
            Member member = mock(Member.class);
            when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.of(member));
            Long amount = 1000L;

            // when
            memberHeartBalanceService.refundPurchasedHearts(memberId, amount);

            // then
            verify(member).refundPurchaseHeart(amount);
        }
    }
}