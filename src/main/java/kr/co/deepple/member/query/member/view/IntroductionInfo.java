package kr.co.deepple.member.query.member.view;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.recommendation.domain.IntroductionType;

public record IntroductionInfo(
    @Schema(implementation = IntroductionType.class) String introductionType) {}
