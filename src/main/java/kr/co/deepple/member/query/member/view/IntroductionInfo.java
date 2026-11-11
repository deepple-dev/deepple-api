package kr.co.deepple.member.query.member.view;

import kr.co.deepple.member.command.domain.introduction.IntroductionType;
import io.swagger.v3.oas.annotations.media.Schema;

public record IntroductionInfo(
    @Schema(implementation = IntroductionType.class)
    String introductionType
) {
}
