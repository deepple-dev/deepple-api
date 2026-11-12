package kr.co.deepple.payment.command.application.heartpurchaseoption;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.command.domain.PaymentErrorCode;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import kr.co.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import kr.co.deepple.payment.fixture.HeartPurchaseOptionFixture;
import kr.co.deepple.payment.presentation.heartpurchaseoption.dto.HeartPurchaseOptionCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeartPurchaseOptionServiceTest {

  @InjectMocks private HeartPurchaseOptionService heartPurchaseOptionService;

  @Mock private HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

  @Nested
  @DisplayName("create 메서드 테스트")
  class CreateMethodTest {

    String productId = "productId";
    Long heartAmount = 100L;
    Long price = 1000L;
    String name = "name";

    @Test
    @DisplayName("이미 존재하는 productId로 생성 요청 시 예외 발생")
    void throwExceptionWhenProductIdAlreadyExists() {
      // Given
      HeartPurchaseOptionCreateRequest request =
          new HeartPurchaseOptionCreateRequest(heartAmount, price, productId, name);
      when(heartPurchaseOptionCommandRepository.existsByProductIdAndDeletedAtIsNull(
              request.productId()))
          .thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> heartPurchaseOptionService.create(request))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", PaymentErrorCode.HEART_PURCHASE_OPTION_ALREADY_EXISTS);
      verify(heartPurchaseOptionCommandRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 productId로 생성 요청 시 정상 처리")
    void createHeartPurchaseOption() {
      // Given
      HeartPurchaseOptionCreateRequest request =
          new HeartPurchaseOptionCreateRequest(heartAmount, price, productId, name);
      when(heartPurchaseOptionCommandRepository.existsByProductIdAndDeletedAtIsNull(
              request.productId()))
          .thenReturn(false);

      // When
      heartPurchaseOptionService.create(request);

      // Then
      verify(heartPurchaseOptionCommandRepository)
          .save(
              argThat(heartPurchaseOption ->
                  heartPurchaseOption.getHeartAmount().equals(heartAmount)
                      && heartPurchaseOption.getPrice().getValue().equals(price)
                      && heartPurchaseOption.getProductId().equals(productId)
                      && heartPurchaseOption.getName().equals(name)));
    }
  }

  @Nested
  @DisplayName("delete 메서드 테스트")
  class DeleteMethodTest {

    Long id = 1L;
    String productId = "productId";

    @Test
    @DisplayName("존재하지 않는 id로 삭제 요청 시 예외 발생")
    void throwExceptionWhenProductIdNotExists() {
      // Given
      when(heartPurchaseOptionCommandRepository.findById(id)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> heartPurchaseOptionService.delete(id))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", PaymentErrorCode.HEART_PURCHASE_OPTION_NOT_FOUND);
      verify(heartPurchaseOptionCommandRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("이미 삭제된 id로 삭제 요청 시 예외 발생")
    void throwExceptionWhenProductIdAlreadyDeleted() {
      // Given
      HeartPurchaseOption heartPurchaseOption = HeartPurchaseOptionFixture.builder().build();
      heartPurchaseOption.delete();
      when(heartPurchaseOptionCommandRepository.findById(id))
          .thenReturn(Optional.of(heartPurchaseOption));

      // When & Then
      assertThatThrownBy(() -> heartPurchaseOptionService.delete(id))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", PaymentErrorCode.HEART_PURCHASE_OPTION_NOT_FOUND);
      verify(heartPurchaseOptionCommandRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("존재하는 id로 삭제 요청 시 정상 처리")
    void deleteHeartPurchaseOption() {
      // Given
      HeartPurchaseOption heartPurchaseOption = HeartPurchaseOptionFixture.builder().build();
      when(heartPurchaseOptionCommandRepository.findById(id))
          .thenReturn(Optional.of(heartPurchaseOption));

      // When
      heartPurchaseOptionService.delete(id);

      // Then
      verify(heartPurchaseOptionCommandRepository).deleteById(id);
    }
  }
}
