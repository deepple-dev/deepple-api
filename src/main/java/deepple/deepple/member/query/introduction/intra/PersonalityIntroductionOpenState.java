package deepple.deepple.member.query.introduction.intra;

import deepple.deepple.datingexam.domain.AnswerPersonalityType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 유형별 이상형 오픈 상태. 오픈 시점에 확정된 유형, 고정 후보 id 목록, 오픈 시각을 담는다.
 * Redis에 JSON으로 직렬화되어 24시간 동안 유지된다.
 */
public record PersonalityIntroductionOpenState(
    AnswerPersonalityType type,
    List<Long> memberIds,
    LocalDateTime openedAt
) {
}
