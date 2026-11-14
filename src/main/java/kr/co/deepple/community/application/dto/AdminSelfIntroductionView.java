package kr.co.deepple.community.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AdminSelfIntroductionView(
    long selfIntroductionId,
    String nickname,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "MALE") String gender,
    boolean isOpened,
    String content,
    String imageUrl,
    String createdDate,
    String updatedDate,
    String deletedDate) {

  public static AdminSelfIntroductionView of(
      RawAdminSelfIntroductionView raw, String nickname, String gender) {
    return new AdminSelfIntroductionView(
        raw.id(),
        nickname,
        gender,
        raw.isOpened(),
        raw.content(),
        raw.imageUrl(),
        formatDate(raw.createdAt()),
        formatDate(raw.updatedAt()),
        formatDate(raw.deletedAt()));
  }

  private static String formatDate(LocalDateTime dateTime) {
    return dateTime != null ? DateTimeFormatter.ofPattern("yyyy/MM/dd").format(dateTime) : null;
  }
}
