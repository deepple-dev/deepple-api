package deepple.deepple.member.query.member.view;

import java.time.LocalDateTime;
import java.util.Set;

public record BasicMemberInfo(
    Long id,
    String nickname,
    String profileImageUrl,
    Integer yearOfBirth,
    String gender,
    Integer height,
    String job,
    Set<String> hobbies,
    String mbti,
    String city,
    String district,
    String smokingStatus,
    String drinkingStatus,
    String highestEducation,
    String religion,
    String like,
    LocalDateTime lastAccessedAt
) {
}
