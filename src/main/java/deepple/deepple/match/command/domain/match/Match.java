package deepple.deepple.match.command.domain.match;

import deepple.deepple.common.entity.BaseEntity;
import deepple.deepple.common.event.Events;
import deepple.deepple.match.command.domain.match.event.*;
import deepple.deepple.match.command.domain.match.exception.InvalidMatchStatusChangeException;
import deepple.deepple.match.command.domain.match.vo.Message;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_responder_id", columnList = "responderId"),
    @Index(name = "idx_requester_id_responder_id", columnList = "requesterId, responderId")
})
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
@Getter
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requesterId;

    private Long responderId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "request_message"))
    private Message requestMessage;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "response_message"))
    private Message responseMessage;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private MatchStatus status;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private MatchContactType requesterContactType;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private MatchContactType responderContactType;

    private LocalDateTime readByResponderAt;

    public static Match request(long requesterId, long responderId, @NonNull Message requestMessage,
        @NonNull String requesterName, @NonNull MatchType type, @NonNull MatchContactType contactType) {
        Match match = Match.builder()
            .requesterId(requesterId)
            .responderId(responderId)
            .requestMessage(requestMessage)
            .status(MatchStatus.WAITING)
            .requesterContactType(contactType)
            .build();

        Events.raise(MatchRequestedEvent.of(requesterId, requesterName, responderId, type.name(), contactType.name()));
        Events.raise(MatchRequestCompletedEvent.of(requesterId, responderId));

        return match;
    }

    public void approve(@NonNull Message message, String responderName, @NonNull MatchContactType contactType) {
        validateChangeStatus();
        status = MatchStatus.MATCHED;
        responseMessage = message;
        responderContactType = contactType;
        Events.raise(MatchRespondedEvent.of(requesterId, responderId, status, contactType.name()));
        Events.raise(MatchAcceptedEvent.of(requesterId, responderId, responderName));
    }

    public void reject(String responderName) {
        validateChangeStatus();
        status = MatchStatus.REJECTED;
        Events.raise(MatchRespondedEvent.of(requesterId, responderId, status, null));
        Events.raise(MatchRejectedEvent.of(requesterId, responderId, responderName));
    }

    public void expire() {
        validateChangeStatus();
        status = MatchStatus.EXPIRED;
        Events.raise(MatchRespondedEvent.of(requesterId, responderId, status, null));
    }

    public void checkRejected() {
        validateChangeRejectChecked();
        status = MatchStatus.REJECT_CHECKED;
    }

    public void read(@NonNull Long readerId) {
        if (readByResponderAt != null) {
            return;
        }
        if (!readerId.equals(responderId)) {
            return;
        }
        readByResponderAt = LocalDateTime.now();
    }

    private void validateChangeStatus() {
        if (status != MatchStatus.WAITING) {
            throw new InvalidMatchStatusChangeException();
        }
    }

    private void validateChangeRejectChecked() {
        if (status != MatchStatus.REJECTED) {
            throw new InvalidMatchStatusChangeException();
        }
    }
}
