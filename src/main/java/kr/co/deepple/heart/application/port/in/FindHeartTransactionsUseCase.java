package kr.co.deepple.heart.application.port.in;

import kr.co.deepple.heart.application.dto.HeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.HeartTransactionViews;

public interface FindHeartTransactionsUseCase {
  HeartTransactionViews findHeartTransactions(
      long memberId, HeartTransactionSearchCondition condition);
}
