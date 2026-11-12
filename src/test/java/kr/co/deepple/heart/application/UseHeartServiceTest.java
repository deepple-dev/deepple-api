package kr.co.deepple.heart.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.heart.application.port.in.UseHeartCommand;
import kr.co.deepple.heart.application.port.out.HeartMemberRepository;
import kr.co.deepple.heart.application.port.out.HeartUsagePolicyRepository;
import kr.co.deepple.heart.domain.Gender;
import kr.co.deepple.heart.domain.TransactionType;
import kr.co.deepple.heart.fixture.HeartMemberFixture;
import kr.co.deepple.heart.fixture.HeartUsagePolicyFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UseHeartServiceTest {

  private static final long MEMBER_ID = 1L;
  private static final long PRICE = 20L;

  @Mock private HeartMemberRepository heartMemberRepository;

  @Mock private HeartUsagePolicyRepository heartUsagePolicyRepository;

  @Mock private HeartTransactor heartTransactor;

  @InjectMocks private UseHeartService useHeartService;

  private void givenHeartMember(boolean isVip) {
    when(heartMemberRepository.findByMemberId(MEMBER_ID))
        .thenReturn(
            Optional.of(
                HeartMemberFixture.builder()
                    .memberId(MEMBER_ID)
                    .gender(Gender.MALE)
                    .isVip(isVip)
                    .build()));
  }

  private void givenUsagePolicy() {
    when(heartUsagePolicyRepository.findByGenderAndTransactionType(
            Gender.MALE, TransactionType.MESSAGE))
        .thenReturn(Optional.of(HeartUsagePolicyFixture.builder().price(PRICE).build()));
  }

  private UseHeartCommand messageCommand(String subtype, String content) {
    return new UseHeartCommand(MEMBER_ID, "MESSAGE", subtype, content, "100");
  }

  @Test
  @DisplayName("정책 가격과 멱등 키로 거래한다. 내용이 없으면 거래 종류의 설명을 쓴다.")
  void transactWithPolicyPrice() {
    // given
    givenHeartMember(false);
    givenUsagePolicy();

    // when
    useHeartService.useHeart(messageCommand("MATCH", null));

    // then
    verify(heartTransactor)
        .transact(
            MEMBER_ID,
            TransactionType.MESSAGE,
            TransactionType.MESSAGE.getDescription(),
            PRICE,
            "100");
  }

  @Test
  @DisplayName("내용을 주면 그 내용으로 거래한다.")
  void transactWithGivenContent() {
    // given
    givenHeartMember(false);
    givenUsagePolicy();

    // when
    useHeartService.useHeart(messageCommand("MATCH", "메시지"));

    // then
    verify(heartTransactor).transact(MEMBER_ID, TransactionType.MESSAGE, "메시지", PRICE, "100");
  }

  @Test
  @DisplayName("VIP 회원은 거래하지 않고 무료로 처리한다.")
  void freeForVip() {
    // given
    givenHeartMember(true);
    givenUsagePolicy();

    // when
    useHeartService.useHeart(messageCommand("MATCH", null));

    // then
    verifyNoInteractions(heartTransactor);
  }

  @Test
  @DisplayName("무료 세부 종류는 거래하지 않는다.")
  void freeForFreeSubtype() {
    // given
    givenHeartMember(false);
    givenUsagePolicy();

    // when
    useHeartService.useHeart(messageCommand("SOULMATE", null));

    // then
    verifyNoInteractions(heartTransactor);
  }

  @Test
  @DisplayName("회원 사본이 없으면 예외가 발생한다.")
  void throwWhenHeartMemberMissing() {
    // given
    when(heartMemberRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> useHeartService.useHeart(messageCommand("MATCH", null)))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("사용 정책이 없으면 예외가 발생한다.")
  void throwWhenUsagePolicyMissing() {
    // given
    givenHeartMember(false);
    when(heartUsagePolicyRepository.findByGenderAndTransactionType(
            Gender.MALE, TransactionType.MESSAGE))
        .thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> useHeartService.useHeart(messageCommand("MATCH", null)))
        .isInstanceOf(IllegalStateException.class);
  }
}
