package kr.co.deepple.payment.application.port.out;

import kr.co.deepple.payment.application.dto.HeartPurchaseOptionSearchCondition;
import kr.co.deepple.payment.application.dto.HeartPurchaseOptionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HeartPurchaseOptionQueryRepository {
  Page<HeartPurchaseOptionView> findPage(
      HeartPurchaseOptionSearchCondition condition, Pageable pageable);
}
