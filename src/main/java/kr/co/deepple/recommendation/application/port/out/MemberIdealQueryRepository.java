package kr.co.deepple.recommendation.application.port.out;

import java.util.Optional;
import kr.co.deepple.recommendation.application.dto.MemberIdealView;

public interface MemberIdealQueryRepository {
  Optional<MemberIdealView> findMemberIdealByMemberId(long memberId);
}
