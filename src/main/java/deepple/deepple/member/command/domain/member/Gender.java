package deepple.deepple.member.command.domain.member;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import lombok.Getter;

@Getter
public enum Gender {
    MALE("남성"),
    FEMALE("여성");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public static Gender from(String value) {
        if (value == null) {
            return null;
        }

        try {
            return Gender.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_OPTION, "Gender=" + value, e);
        }
    }

    public Gender getOpposite() {
        return this == MALE ? FEMALE : MALE;
    }
}
