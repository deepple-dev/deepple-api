package kr.co.deepple.member.presentation.introduction.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.DrinkingStatus;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.member.command.domain.member.Religion;
import kr.co.deepple.member.command.domain.member.SmokingStatus;

public record MemberIdealUpdateRequest(
    @NotNull(message = "최소 나이를 입력해주세요.")
        @Min(value = 20, message = "최소 나이는 20보다 작을 수 없습니다.")
        @Max(value = 46, message = "최소 나이는 46보다 작을 수 없습니다.")
        Integer minAge,
    @NotNull(message = "최대 나이를 입력해주세요.")
        @Min(value = 20, message = "최대 나이는 20보다 작을 수 없습니다.")
        @Max(value = 46, message = "최대 나이는 46보다 작을 수 없습니다.")
        Integer maxAge,
    @ArraySchema(schema = @Schema(implementation = City.class)) @NotNull(message = "지역을 입력해주세요.")
        Set<String> cities,
    @Schema(implementation = Religion.class) String religion,
    @Schema(implementation = SmokingStatus.class) String smokingStatus,
    @Schema(implementation = DrinkingStatus.class) String drinkingStatus,
    @ArraySchema(schema = @Schema(implementation = Hobby.class)) @NotNull(message = "취미를 입력해주세요.")
        Set<String> hobbies) {}
