package kr.co.deepple.match.application.port.out;

import java.util.Set;
import kr.co.deepple.match.application.dto.MatchHistorySearchCondition;
import kr.co.deepple.match.application.dto.RawMatchHistoryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminMatchHistoryQueryRepository {
  Page<RawMatchHistoryView> findMatchHistories(
      MatchHistorySearchCondition condition,
      Set<Long> nicknameMemberIds,
      Set<Long> phoneNumberMemberIds,
      Pageable pageable);
}
