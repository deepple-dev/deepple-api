package kr.co.deepple.mission.command.domain.mission;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActionType {
  LIKE("오늘의 좋아요 보내기 완료"),
  INTERVIEW("인터뷰 최초 작성 완료"),
  FIRST_DATE_EXAM("연애가치관 테스트 완료"),
  SELF_INTRODUCTION("셀프소개 최초 작성 완료");

  private final String description;

  public static ActionType from(String actionType) {
    try {
      return ActionType.valueOf(actionType.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 ActionType 값입니다. value=" + actionType, e);
    }
  }
}
