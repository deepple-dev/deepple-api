package kr.co.deepple.community.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record SelfIntroductionSearchRequest(
    @ArraySchema(schema = @Schema(description = "지역. 값은 회원 프로필의 city와 같다", example = "SEOUL"))
        List<String> preferredCities,
    Integer fromAge,
    Integer toAge,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "MALE")
        @Pattern(regexp = "(?i)MALE|FEMALE", message = "성별은 MALE 또는 FEMALE이어야 합니다.")
        String gender,
    Long lastId) {}
