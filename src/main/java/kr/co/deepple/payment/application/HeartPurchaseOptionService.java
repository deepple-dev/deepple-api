package kr.co.deepple.payment.application;

import jakarta.transaction.Transactional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.payment.application.dto.HeartPurchaseOptionCreateRequest;
import kr.co.deepple.payment.application.port.out.HeartPurchaseOptionCommandRepository;
import kr.co.deepple.payment.domain.HeartPurchaseAmount;
import kr.co.deepple.payment.domain.HeartPurchaseOption;
import kr.co.deepple.payment.domain.PaymentErrorCode;
import kr.co.deepple.payment.domain.Price;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HeartPurchaseOptionService {

  private final HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

  @Transactional
  public void create(HeartPurchaseOptionCreateRequest request) {
    validateCreateRequest(request);
    createHeartPurchaseOption(request);
  }

  @Transactional
  public void delete(long id) {
    validateDeleteRequest(id);
    heartPurchaseOptionCommandRepository.deleteById(id);
  }

  private void validateCreateRequest(HeartPurchaseOptionCreateRequest request) {
    if (heartPurchaseOptionCommandRepository.existsByProductIdAndDeletedAtIsNull(
        request.productId())) {
      throw new DeeppleException(
          PaymentErrorCode.HEART_PURCHASE_OPTION_ALREADY_EXISTS,
          "productId=" + request.productId());
    }
  }

  private void createHeartPurchaseOption(HeartPurchaseOptionCreateRequest request) {
    HeartPurchaseAmount heartAmount = HeartPurchaseAmount.from(request.heartAmount());
    Price price = Price.from(request.price());
    HeartPurchaseOption heartPurchaseOption =
        HeartPurchaseOption.of(heartAmount, price, request.productId(), request.name());
    heartPurchaseOptionCommandRepository.save(heartPurchaseOption);
  }

  private void validateDeleteRequest(long id) {
    HeartPurchaseOption heartPurchaseOption =
        heartPurchaseOptionCommandRepository
            .findById(id)
            .orElseThrow(() ->
                new DeeppleException(
                    PaymentErrorCode.HEART_PURCHASE_OPTION_NOT_FOUND,
                    "heartPurchaseOptionId=" + id));
    if (heartPurchaseOption.isDeleted()) {
      throw new DeeppleException(
          PaymentErrorCode.HEART_PURCHASE_OPTION_NOT_FOUND,
          "이미 삭제된 하트 구매 옵션입니다. heartPurchaseOptionId=" + id);
    }
  }
}
