package kr.co.deepple.heart.application;

import static org.mockito.Mockito.verify;

import kr.co.deepple.heart.application.port.in.AddPurchaseHeartCommand;
import kr.co.deepple.heart.domain.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddPurchaseHeartServiceTest {

  @Mock private HeartTransactor heartTransactor;

  @InjectMocks private AddPurchaseHeartService service;

  @Test
  @DisplayName("구매 종류와 주문 ID 멱등 키로 거래한다.")
  void transact() {
    // when
    service.addPurchaseHeart(new AddPurchaseHeartCommand(1L, 7L, 45L));

    // then
    verify(heartTransactor)
        .transact(
            1L, TransactionType.PURCHASE, TransactionType.PURCHASE.getDescription(), 45L, "7");
  }
}
