package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.dto.HeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.HeartTransactionView;
import kr.co.deepple.heart.application.dto.HeartTransactionViews;
import kr.co.deepple.heart.application.port.out.HeartTransactionQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindHeartTransactionsServiceTest {

    @InjectMocks
    private FindHeartTransactionsService findHeartTransactionsService;

    @Mock
    private HeartTransactionQueryRepository heartTransactionQueryRepository;

    @Test
    @DisplayName("하트 내역 조회 서비스 테스트")
    void findHeartTransactionsTest() {
        // Given
        long memberId = 1L;
        int size = 13;
        HeartTransactionSearchCondition condition = new HeartTransactionSearchCondition(null);
        List<HeartTransactionView> views = createHeartTransactionViews(size);
        when(heartTransactionQueryRepository.findHeartTransactions(memberId, condition, size)).thenReturn(views);

        // When
        final HeartTransactionViews result = findHeartTransactionsService.findHeartTransactions(memberId, condition);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.hasMore()).isTrue();
        assertThat(result.transactions()).hasSize(size - 1);
        for (int i = 0; i < size - 1; i++) {
            HeartTransactionView view = result.transactions().get(i);
            assertThat(view.id()).isEqualTo((long) i);
        }
    }

    @Test
    @DisplayName("하트 내역이 13개 미만인 경우 테스트")
    void findHeartTransactionsLessThanThirteenTest() {
        // Given
        long memberId = 1L;
        int size = 13;
        int actualSize = 12;
        HeartTransactionSearchCondition condition = new HeartTransactionSearchCondition(null);
        List<HeartTransactionView> views = createHeartTransactionViews(actualSize);
        when(heartTransactionQueryRepository.findHeartTransactions(memberId, condition, size)).thenReturn(views);

        // When
        final HeartTransactionViews result = findHeartTransactionsService.findHeartTransactions(memberId, condition);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.hasMore()).isFalse();
        assertThat(result.transactions()).hasSize(actualSize);
        for (int i = 0; i < actualSize; i++) {
            HeartTransactionView view = result.transactions().get(i);
            assertThat(view.id()).isEqualTo((long) i);
        }
    }

    private List<HeartTransactionView> createHeartTransactionViews(int size) {
        List<HeartTransactionView> views = new ArrayList<>();
        for (long i = 0; i < size; i++) {
            views.add(new HeartTransactionView(i, null, null, null));
        }
        return views;
    }

}