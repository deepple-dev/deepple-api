package kr.co.deepple.community.presentation.selfintroduction.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.Gender;

public record SelfIntroductionSearchRequest(
    @ArraySchema(schema = @Schema(implementation = City.class)) List<String> preferredCities,
    Integer fromAge,
    Integer toAge,
    @Schema(implementation = Gender.class) String gender,
    Long lastId) {}
