package kr.co.deepple.heart.adapter.out.persistence;

import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.heart.application.dto.HeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.HeartTransactionView;
import kr.co.deepple.heart.domain.HeartTransaction;
import kr.co.deepple.heart.domain.TransactionType;
import kr.co.deepple.heart.fixture.HeartTransactionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class HeartTransactionQueryRepositoryImplTest extends RepositoryTestSupport {

    @Autowired
    private HeartTransactionQueryRepositoryImpl heartTransactionQueryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("하트 내역 조회 테스트")
    void findHeartTransactionsTest() {
        // Given
        long memberId = 1L;
        Long lastId = null;
        int size = 10;
        HeartTransactionSearchCondition condition = new HeartTransactionSearchCondition(lastId);

        final HeartTransaction heartTransaction = createHeartTransaction(memberId);
        entityManager.flush();
        entityManager.clear();

        // When
        List<HeartTransactionView> result = heartTransactionQueryRepository.findHeartTransactions(memberId, condition,
            size);

        // Then
        assertThat(result).hasSize(1);
        HeartTransactionView view = result.get(0);
        assertThat(view.id()).isEqualTo(heartTransaction.getId());
        assertThat(view.content()).isEqualTo(heartTransaction.getContent());
        assertThat(view.heartAmount()).isEqualTo(heartTransaction.getHeartAmount().getAmount());
        assertThat(view.createdAt()).isCloseTo(heartTransaction.getCreatedAt(), within(1, ChronoUnit.MICROS));
    }

    @Test
    @DisplayName("하트 내역 조회 시 lastId가 주어지면 해당 ID보다 작은 내역만 조회합니다.")
    void findHeartTransactionsWithLastIdTest() {
        // Given
        long memberId = 1L;
        List<HeartTransaction> heartTransactions = List.of(
            createHeartTransaction(memberId),
            createHeartTransaction(memberId),
            createHeartTransaction(memberId)
        );
        entityManager.flush();
        entityManager.clear();
        heartTransactions.stream().sorted(Comparator.comparing(HeartTransaction::getId).reversed());

        Long lastId = heartTransactions.getLast().getId();
        HeartTransactionSearchCondition condition = new HeartTransactionSearchCondition(lastId);

        // When
        List<HeartTransactionView> result = heartTransactionQueryRepository.findHeartTransactions(memberId, condition,
            10);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(heartTransactions.get(1).getId());
        assertThat(result.get(1).id()).isEqualTo(heartTransactions.get(0).getId());
    }

    @Test
    @DisplayName("하트 내역 조회 시 memberId가 일치하지 않으면 빈 리스트를 반환합니다.")
    void findHeartTransactionsWithNonMatchingMemberIdTest() {
        // Given
        long memberId = 1L;
        createHeartTransaction(memberId);
        entityManager.flush();
        entityManager.clear();
        long nonMatchingMemberId = 2L;
        HeartTransactionSearchCondition condition = new HeartTransactionSearchCondition(null);

        // When
        List<HeartTransactionView> result = heartTransactionQueryRepository.findHeartTransactions(nonMatchingMemberId,
            condition,
            10);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("하트 내역 조회 시 size가 1이면 id값이 가장 큰 1개를 반환합니다.")
    void findHeartTransactionsWithZeroSizeTest() {
        // Given
        long memberId = 1L;
        List<HeartTransaction> heartTransactions = List.of(
            createHeartTransaction(memberId),
            createHeartTransaction(memberId),
            createHeartTransaction(memberId)
        );
        entityManager.flush();
        entityManager.clear();
        HeartTransactionSearchCondition condition = new HeartTransactionSearchCondition(null);

        // When
        List<HeartTransactionView> result = heartTransactionQueryRepository.findHeartTransactions(memberId, condition,
            1);

        // Then
        assertThat(result).hasSize(1);
        var view = result.get(0);
        assertThat(view.id()).isEqualTo(
            heartTransactions.stream().max(Comparator.comparing(HeartTransaction::getId)).get().getId());
    }

    @Test
    @DisplayName("하트 내역 조회 시 size가 0 이하이면 예외를 던집니다.")
    void findHeartTransactionsWithZeroOrNegativeSizeTest() {
        // Given
        long memberId = 1L;
        HeartTransactionSearchCondition condition = new HeartTransactionSearchCondition(null);

        // When & Then
        assertThatThrownBy(() -> heartTransactionQueryRepository.findHeartTransactions(memberId, condition, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private HeartTransaction createHeartTransaction(final long memberId) {
        final HeartTransaction heartTransaction = HeartTransactionFixture.builder()
            .memberId(memberId)
            .transactionType(TransactionType.PURCHASE)
            .content(TransactionType.PURCHASE.getDescription())
            .amount(10L)
            .missionHeartBalance(100L)
            .purchaseHeartBalance(100L)
            .build();
        entityManager.persist(heartTransaction);
        return heartTransaction;
    }
}
