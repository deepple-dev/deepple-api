package kr.co.deepple.match.command.domain.match;

import kr.co.deepple.common.exception.DeeppleException;

public enum MatchContactType {
    PHONE_NUMBER,
    KAKAO;

    public static MatchContactType from(String value) {
        try {
            return MatchContactType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new DeeppleException(MatchErrorCode.INVALID_MATCH_CONTACT_TYPE, "value=" + value, e);
        }
    }
}
