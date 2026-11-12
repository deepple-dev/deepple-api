package kr.co.deepple.member.query.introduction.application;

import com.querydsl.core.annotations.QueryProjection;
import java.util.Set;

public record MemberIdealView(
    Integer minAge,
    Integer maxAge,
    Set<String> hobbies,
    Set<String> cities,
    String religion,
    String smokingStatus,
    String drinkingStatus) {
  @QueryProjection
  public MemberIdealView {}
}
