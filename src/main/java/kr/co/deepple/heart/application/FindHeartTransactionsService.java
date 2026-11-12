package kr.co.deepple.heart.application;

import kr.co.deepple.heart.application.dto.HeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.HeartTransactionView;
import kr.co.deepple.heart.application.dto.HeartTransactionViews;
import kr.co.deepple.heart.application.port.in.FindHeartTransactionsUseCase;
import kr.co.deepple.heart.application.port.out.HeartTransactionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindHeartTransactionsService implements FindHeartTransactionsUseCase {
    private static final int PAGE_SIZE = 12;

    private final HeartTransactionQueryRepository heartTransactionQueryRepository;

    @Override
    public HeartTransactionViews findHeartTransactions(long memberId, HeartTransactionSearchCondition condition) {
        List<HeartTransactionView> views = heartTransactionQueryRepository.findHeartTransactions(memberId,
            condition, PAGE_SIZE + 1);
        boolean hasMore = views.size() > PAGE_SIZE;
        List<HeartTransactionView> transactions = views.stream()
            .limit(PAGE_SIZE)
            .toList();

        return new HeartTransactionViews(transactions, hasMore);
    }
}
