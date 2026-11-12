package kr.co.deepple.heart.application.port.out;

import java.util.List;
import kr.co.deepple.heart.application.dto.HeartTransactionSearchCondition;
import kr.co.deepple.heart.application.dto.HeartTransactionView;

public interface HeartTransactionQueryRepository {
  List<HeartTransactionView> findHeartTransactions(
      long memberId, HeartTransactionSearchCondition condition, int size);
}
