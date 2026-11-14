package kr.co.deepple.recommendation.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record MemberIdealUpdateRequest(
    @NotNull(message = "최소 나이를 입력해주세요.")
        @Min(value = 20, message = "최소 나이는 20보다 작을 수 없습니다.")
        @Max(value = 46, message = "최소 나이는 46보다 작을 수 없습니다.")
        Integer minAge,
    @NotNull(message = "최대 나이를 입력해주세요.")
        @Min(value = 20, message = "최대 나이는 20보다 작을 수 없습니다.")
        @Max(value = 46, message = "최대 나이는 46보다 작을 수 없습니다.")
        Integer maxAge,
    @ArraySchema(schema = @Schema(description = "지역. 값은 회원 프로필의 city와 같다", example = "SEOUL"))
        @NotNull(message = "지역을 입력해주세요.")
        Set<String> cities,
    @Schema(description = "종교. 값은 회원 프로필의 religion과 같다", example = "NONE") String religion,
    @Schema(description = "흡연 여부. 값은 회원 프로필의 smokingStatus와 같다", example = "NONE")
        String smokingStatus,
    @Schema(description = "음주 여부. 값은 회원 프로필의 drinkingStatus와 같다", example = "SOCIAL")
        String drinkingStatus,
    @ArraySchema(schema = @Schema(description = "취미. 값은 회원 프로필의 hobbies와 같다", example = "TRAVEL"))
        @NotNull(message = "취미를 입력해주세요.")
        Set<String> hobbies) {}
