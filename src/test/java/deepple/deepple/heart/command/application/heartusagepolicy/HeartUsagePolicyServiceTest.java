package deepple.deepple.heart.command.application.heartusagepolicy;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.heart.command.domain.hearttransaction.HeartTransaction;
import deepple.deepple.heart.command.domain.hearttransaction.HeartTransactionCommandRepository;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionSubtype;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import deepple.deepple.heart.command.domain.heartusagepolicy.HeartPriceAmount;
import deepple.deepple.heart.command.domain.heartusagepolicy.HeartUsagePolicy;
import deepple.deepple.heart.command.domain.heartusagepolicy.HeartUsagePolicyCommandRepository;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Gender;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class HeartUsagePolicyServiceTest {
    @InjectMocks
    private HeartUsagePolicyService heartUsageService;
    @Mock
    private HeartUsagePolicyCommandRepository heartUsagePolicyCommandRepository;
    @Mock
    private HeartTransactionCommandRepository heartTransactionCommandRepository;
    @Mock
    private MemberCommandRepository memberCommandRepository;

    @Test
    @DisplayName("멤버가 존재하지 않는 경우 예외 발생")
    void shouldThrowExceptionWhenMemberNotFound() {
        // given
        Long memberId = 1L;
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());
        TransactionType transactionType = TransactionType.MESSAGE;
        String content = transactionType.getDescription();
        TransactionSubtype subtype = TransactionSubtype.MATCH;

        // when & then
        assertThatThrownBy(() -> heartUsageService.useHeart(memberId, transactionType, content, subtype.name()))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("하트 사용 정책이 없는 경우 예외 발생")
    void shouldThrowExceptionWhenHeartUsagePolicyNotFound() {
        // given
        Member member = Member.fromPhoneNumber("01012345678");
        Long memberId = 1L;
        setField(member, "id", memberId);
        Gender gender = Gender.MALE;
        MemberProfile memberProfile = MemberProfile.builder()
            .gender(gender)
            .build();
        setField(member, "profile", memberProfile);
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));
        TransactionType transactionType = TransactionType.MESSAGE;
        String content = transactionType.getDescription();
        when(heartUsagePolicyCommandRepository.findByGenderAndTransactionType(gender, transactionType))
            .thenReturn(Optional.empty());

        TransactionSubtype subtype = TransactionSubtype.MATCH;

        // when & then
        assertThatThrownBy(
            () -> heartUsageService.useHeart(memberId, transactionType, content, subtype.name()))
            .isInstanceOf(IllegalStateException.class);

        verify(heartUsagePolicyCommandRepository, atMostOnce()).findByGenderAndTransactionType(gender, transactionType);
        verify(heartTransactionCommandRepository, never()).save(any(HeartTransaction.class));
    }

    @Test
    @DisplayName("하트 사용량이 0인 경우 경우 하트 사용 처리하지 않음")
    void shouldNotProcessHeartUsageWhenHeartAmountIsZero() {
        // given
        Member member = Member.fromPhoneNumber("01012345678");
        // vip는 하트 사용량 0
        setField(member, "isVip", true);
        Long memberId = 1L;
        setField(member, "id", memberId);
        Gender gender = Gender.MALE;
        MemberProfile memberProfile = MemberProfile.builder()
            .gender(gender)
            .build();
        setField(member, "profile", memberProfile);
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

        HeartBalance heartBalanceBeforeUsingHeart = HeartBalance.init();
        setField(heartBalanceBeforeUsingHeart, "purchaseHeartBalance", 100L);
        setField(heartBalanceBeforeUsingHeart, "missionHeartBalance", 100L);
        setField(member, "heartBalance", heartBalanceBeforeUsingHeart);
        TransactionType transactionType = TransactionType.MESSAGE;
        HeartPriceAmount heartPriceAmount = HeartPriceAmount.from(10L);
        HeartUsagePolicy heartUsagePolicy = HeartUsagePolicy.of(transactionType, gender, heartPriceAmount);

        when(heartUsagePolicyCommandRepository.findByGenderAndTransactionType(gender, transactionType))
            .thenReturn(Optional.of(heartUsagePolicy));
        TransactionSubtype subtype = TransactionSubtype.MATCH;

        // when
        heartUsageService.useHeart(memberId, transactionType,
            transactionType.getDescription(), subtype.name());

        // then
        verify(heartUsagePolicyCommandRepository, atMostOnce()).findByGenderAndTransactionType(gender, transactionType);
        verify(heartTransactionCommandRepository, never()).save(any(HeartTransaction.class));
    }

    @Test
    @DisplayName("VIP 멤버가 아닌 경우 하트 사용 정책에 따라 하트 사용량 계산")
    void shouldCalculateHeartAmountAccordingToHeartUsagePolicy() {
        // given
        Member member = Member.fromPhoneNumber("01012345678");
        Long memberId = 1L;
        setField(member, "id", memberId);
        Gender gender = Gender.MALE;
        MemberProfile memberProfile = MemberProfile.builder()
            .gender(gender)
            .build();
        setField(member, "profile", memberProfile);
        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

        HeartBalance heartBalanceBeforeUsingHeart = HeartBalance.init();
        setField(heartBalanceBeforeUsingHeart, "purchaseHeartBalance", 100L);
        setField(heartBalanceBeforeUsingHeart, "missionHeartBalance", 100L);
        setField(member, "heartBalance", heartBalanceBeforeUsingHeart);
        TransactionType transactionType = TransactionType.MESSAGE;
        HeartPriceAmount heartPriceAmount = HeartPriceAmount.from(10L);
        HeartUsagePolicy heartUsagePolicy = HeartUsagePolicy.of(transactionType, gender, heartPriceAmount);

        when(heartUsagePolicyCommandRepository.findByGenderAndTransactionType(gender, transactionType))
            .thenReturn(Optional.of(heartUsagePolicy));
        when(heartTransactionCommandRepository.save(any(HeartTransaction.class)))
            .thenAnswer(invocation -> {
                HeartTransaction heartTransaction = invocation.getArgument(0);
                setField(heartTransaction, "id", 1L);
                return heartTransaction;
            });
        TransactionSubtype subtype = TransactionSubtype.MATCH;

        // when
        heartUsageService.useHeart(memberId, transactionType,
            transactionType.getDescription(), subtype.name());

        // then
        verify(heartUsagePolicyCommandRepository, atMostOnce()).findByGenderAndTransactionType(gender, transactionType);
        verify(heartTransactionCommandRepository, atMostOnce()).save(any(HeartTransaction.class));
    }
}
