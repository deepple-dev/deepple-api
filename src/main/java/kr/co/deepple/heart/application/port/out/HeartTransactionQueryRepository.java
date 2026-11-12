package kr.co.deepple.heart.application.port.out;

import kr.co.deepple.heart.application.dto.HeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.HeartTransactionView;

import java.util.List;

public interface HeartTransactionQueryRepository {
    List<HeartTransactionView> findHeartTransactions(long memberId, HeartTransactionSearchCondition condition,
        int size);
}
