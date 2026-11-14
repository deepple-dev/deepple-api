package kr.co.deepple.community.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record SelfIntroductionView(
    MemberBasicInfo memberBasicInfo,
    @Schema(
            description = "조회한 회원이 작성자에게 보낸 좋아요 단계. 값은 좋아요의 level과 같고, 보내지 않았으면 null이다",
            example = "INTERESTED")
        String like,
    String title,
    String content,
    String imageUrl,
    String profileExchangeStatus,
    LocalDateTime createdAt) {}
