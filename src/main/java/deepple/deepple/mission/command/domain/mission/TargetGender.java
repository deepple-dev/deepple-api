package deepple.deepple.mission.command.domain.mission;

import lombok.Getter;

@Getter
public enum TargetGender {
    MALE("남성"),
    FEMALE("여성"),
    ALL("모두");

    private final String description;

    TargetGender(String description) {
        this.description = description;
    }

    public static TargetGender from(String targetGender) {
        try {
            return TargetGender.valueOf(targetGender.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 TargetGender 값입니다. value=" + targetGender, e);
        }
    }
}
