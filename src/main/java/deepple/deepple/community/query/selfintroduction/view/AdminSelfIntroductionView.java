package deepple.deepple.community.query.selfintroduction.view;

import com.querydsl.core.annotations.QueryProjection;
import deepple.deepple.member.command.domain.member.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AdminSelfIntroductionView(
    long selfIntroductionId,
    String nickname,
    @Schema(implementation = Gender.class)
    String gender,
    boolean isOpened,
    String content,
    String imageUrl,
    String createdDate,
    String updatedDate,
    String deletedDate
) {
    @QueryProjection
    public AdminSelfIntroductionView(
        long selfIntroductionId,
        String nickname,
        String gender,
        boolean isOpened,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
    ) {
        this(
            selfIntroductionId,
            nickname,
            gender,
            isOpened,
            content,
            imageUrl,
            formatDate(createdAt),
            formatDate(updatedAt),
            formatDate(deletedAt)
        );
    }


    private static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? DateTimeFormatter.ofPattern("yyyy/MM/dd").format(dateTime) : null;
    }
}
