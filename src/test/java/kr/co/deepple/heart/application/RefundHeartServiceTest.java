package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.port.in.RefundHeartCommand;
import kr.co.deepple.heart.domain.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefundHeartServiceTest {
    @Mock
    private HeartTransactor heartTransactor;

    @InjectMocks
    private RefundHeartService service;

    @Test
    @DisplayName("환불 종류와 주문 ID 멱등 키로 거래한다.")
    void transact() {
        // when
        service.refundHeart(new RefundHeartCommand(1L, 7L, 45L));

        // then
        verify(heartTransactor).transact(1L, TransactionType.REFUND, TransactionType.REFUND.getDescription(), 45L, "7");
    }
}
