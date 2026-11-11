package kr.co.deepple.match.fixture;

import kr.co.deepple.match.command.domain.match.Match;
import kr.co.deepple.match.command.domain.match.MatchContactType;
import kr.co.deepple.match.command.domain.match.MatchStatus;
import kr.co.deepple.match.command.domain.match.vo.Message;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchFixture {

    private Long id;
    private long requesterId = 1L;
    private long responderId = 2L;
    private String requestMessage = "안녕하세요";
    private String responseMessage;
    private MatchStatus status = MatchStatus.WAITING;
    private MatchContactType requesterContactType = MatchContactType.PHONE_NUMBER;
    private MatchContactType responderContactType;
    private LocalDateTime readByResponderAt;

    public static MatchFixture builder() {
        return new MatchFixture();
    }

    public Match build() {
        Match match = instantiateClass(Match.class);
        setField(match, "id", id);
        setField(match, "requesterId", requesterId);
        setField(match, "responderId", responderId);
        setField(match, "requestMessage", requestMessage == null ? null : Message.from(requestMessage));
        setField(match, "responseMessage", responseMessage == null ? null : Message.from(responseMessage));
        setField(match, "status", status);
        setField(match, "requesterContactType", requesterContactType);
        setField(match, "responderContactType", responderContactType);
        setField(match, "readByResponderAt", readByResponderAt);
        return match;
    }
}
