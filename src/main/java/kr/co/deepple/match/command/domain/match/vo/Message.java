package kr.co.deepple.match.command.domain.match.vo;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.match.command.domain.match.MatchErrorCode;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static lombok.AccessLevel.PROTECTED;

@Embeddable
@NoArgsConstructor(access = PROTECTED, force = true)
public class Message {

    private static final int MAX_LENGTH = 200;

    @Getter
    private String value;

    private Message(@NonNull String value) {
        validate(value);
        this.value = value;
    }

    public static Message from(String message) {
        return new Message(message);
    }

    private void validate(String value) {
        if (value.trim().isEmpty()) {
            throw new DeeppleException(MatchErrorCode.INVALID_MATCH_MESSAGE);
        }
        int length = value.codePointCount(0, value.length());
        if (length > MAX_LENGTH) {
            throw new DeeppleException(MatchErrorCode.MATCH_MESSAGE_TOO_LONG, "length=" + length);
        }
    }
}
