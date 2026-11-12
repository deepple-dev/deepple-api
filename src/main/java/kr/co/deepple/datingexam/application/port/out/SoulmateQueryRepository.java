package kr.co.deepple.datingexam.application.port.out;

import java.util.Set;

public interface SoulmateQueryRepository {
    Set<Long> findSameAnswerMemberIds(Long memberId);
}
