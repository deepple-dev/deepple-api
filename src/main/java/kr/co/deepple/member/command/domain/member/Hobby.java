package kr.co.deepple.member.command.domain.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.Getter;

@Getter
public enum Hobby {
  TRAVEL("국내여행/해외여행"),
  PERFORMANCE_AND_EXHIBITION("공연/전시회관람"),
  WEBTOON_AND_COMICS("웹툰/만화"),
  DRAMA_AND_ENTERTAINMENT("드라마/예능보기"),
  PC_AND_MOBILE_GAMES("PC/모바일게임"),
  ANIMATION("애니메이션"),
  GOLF("골프"),
  THEATER_AND_MOVIES("연극/영화"),
  WRITING("글쓰기"),
  BOARD_GAMES("보드게임"),
  PHOTOGRAPHY("사진촬영"),
  SINGING("노래"),
  BADMINTON_AND_TENNIS("배드민턴/테니스"),
  DANCE("댄스"),
  DRIVING("드라이브"),
  HIKING_AND_CLIMBING("등산/클라이밍"),
  WALKING("산책"),
  FOOD_HUNT("맛집탐방"),
  SHOPPING("쇼핑"),
  SKI_AND_SNOWBOARD("스키/스노우보드"),
  PLAYING_INSTRUMENTS("악기연주"),
  WINE("와인"),
  WORKOUT("운동/헬스"),
  YOGA_AND_PILATES("요가/필라테스"),
  COOKING("요리"),
  INTERIOR_DESIGN("인테리어"),
  CYCLING("자전거"),
  CAMPING("캠핑"),
  OTHERS("기타");

  private final String description;

  Hobby(String description) {
    this.description = description;
  }

  public static Hobby from(String value) {
    if (value == null) {
      return null;
    }
    try {
      return Hobby.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_OPTION, "Hobby=" + value, e);
    }
  }
}
