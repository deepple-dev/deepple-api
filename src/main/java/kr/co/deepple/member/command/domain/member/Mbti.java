package kr.co.deepple.member.command.domain.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.Getter;

@Getter
public enum Mbti {
    ESFP, ESFJ, ESTP, ESTJ,
    ENFP, ENFJ, ENTP, ENTJ,
    ISFP, ISFJ, ISTP, ISTJ,
    INFP, INFJ, INTP, INTJ;

    public static Mbti from(String value) {
        if (value == null) {
            return null;
        }

        try {
            return Mbti.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_OPTION, "Mbti=" + value, e);
        }
    }
}
