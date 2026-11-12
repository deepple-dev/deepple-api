package kr.co.deepple.member.query.member.view;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.member.command.domain.member.Gender;

public record BasicInfo(
    String nickname,
    @Schema(implementation = Gender.class) String gender,
    String kakaoId,
    Integer age,
    Integer height,
    String phoneNumber) {
  @QueryProjection
  public BasicInfo {}
}
