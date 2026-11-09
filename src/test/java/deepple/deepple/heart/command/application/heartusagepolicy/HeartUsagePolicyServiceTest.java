package deepple.deepple.heart.command.application.heartusagepolicy;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.heart.command.domain.hearttransaction.HeartTransaction;
import deepple.deepple.heart.command.domain.hearttransaction.HeartTransactionCommandRepository;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionSubtype;
import deepple.deepple.heart.command.domain.hearttransaction.vo.TransactionType;
import deepple.deepple.heart.command.domain.heartusagepolicy.HeartUsagePolicy;
import deepple.deepple.heart.command.domain.heartusagepolicy.HeartUsagePolicyCommandRepository;
import deepple.deepple.heart.fixture.HeartUsagePolicyFixture;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Gender;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.fixture.MemberFixture;
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
        when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.empty());
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
        Long memberId = 1L;
        Gender gender = Gender.MALE;
        Member member = MemberFixture.builder()
            .id(memberId)
            .gender(gender)
            .build();
        when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.of(member));
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
        Long memberId = 1L;
        Gender gender = Gender.MALE;
        // vip는 하트 사용량 0
        Member member = MemberFixture.builder()
            .id(memberId)
            .vip(true)
            .gender(gender)
            .purchaseHeartBalance(100L)
            .missionHeartBalance(100L)
            .build();
        when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.of(member));

        TransactionType transactionType = TransactionType.MESSAGE;
        HeartUsagePolicy heartUsagePolicy = HeartUsagePolicyFixture.builder()
            .transactionType(transactionType)
            .gender(gender)
            .price(10L)
            .build();

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
        Long memberId = 1L;
        Gender gender = Gender.MALE;
        Member member = MemberFixture.builder()
            .id(memberId)
            .gender(gender)
            .purchaseHeartBalance(100L)
            .missionHeartBalance(100L)
            .build();
        when(memberCommandRepository.findByIdForUpdate(memberId)).thenReturn(Optional.of(member));

        TransactionType transactionType = TransactionType.MESSAGE;
        HeartUsagePolicy heartUsagePolicy = HeartUsagePolicyFixture.builder()
            .transactionType(transactionType)
            .gender(gender)
            .price(10L)
            .build();

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
