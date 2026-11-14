package kr.co.deepple.member.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import kr.co.deepple.member.domain.City;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.DrinkingStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.HighestEducation;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Job;
import kr.co.deepple.member.domain.Mbti;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.domain.SmokingStatus;

public record MemberProfileView(
    String nickname,
    Integer yearOfBirth,
    @Schema(implementation = Gender.class) String gender,
    Integer height,
    @Schema(implementation = Job.class) String job,
    @Schema(implementation = Hobby.class) Set<String> hobbies,
    @Schema(implementation = Mbti.class) String mbti,
    @Schema(implementation = City.class) String city,
    @Schema(implementation = District.class) String district,
    @Schema(implementation = SmokingStatus.class) String smokingStatus,
    @Schema(implementation = DrinkingStatus.class) String drinkingStatus,
    @Schema(implementation = HighestEducation.class) String highestEducation,
    @Schema(implementation = Religion.class) String religion) {
  @QueryProjection
  public MemberProfileView {}
}
