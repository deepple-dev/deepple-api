package kr.co.deepple.match.command.domain.match;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.match.command.domain.match.vo.Message;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "matches")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Match extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long requesterId;

  private long responderId;

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

  public static Match request(
      long requesterId,
      long responderId,
      @NonNull Message requestMessage,
      @NonNull MatchContactType contactType) {
    return Match.builder()
        .requesterId(requesterId)
        .responderId(responderId)
        .requestMessage(requestMessage)
        .status(MatchStatus.WAITING)
        .requesterContactType(contactType)
        .build();
  }

  public void approve(@NonNull Message message, @NonNull MatchContactType contactType) {
    validateChangeStatus();
    status = MatchStatus.MATCHED;
    responseMessage = message;
    responderContactType = contactType;
  }

  public void reject() {
    validateChangeStatus();
    status = MatchStatus.REJECTED;
  }

  public void expire() {
    validateChangeStatus();
    status = MatchStatus.EXPIRED;
  }

  public void checkRejected() {
    validateChangeRejectChecked();
    status = MatchStatus.REJECT_CHECKED;
  }

  public void read(long readerId) {
    if (readByResponderAt != null) {
      return;
    }
    if (readerId != responderId) {
      return;
    }
    readByResponderAt = LocalDateTime.now();
  }

  private void validateChangeStatus() {
    if (status != MatchStatus.WAITING) {
      throw new DeeppleException(
          MatchErrorCode.INVALID_MATCH_STATUS_CHANGE, "matchId=" + id + ", status=" + status);
    }
  }

  private void validateChangeRejectChecked() {
    if (status != MatchStatus.REJECTED) {
      throw new DeeppleException(
          MatchErrorCode.INVALID_MATCH_STATUS_CHANGE, "matchId=" + id + ", status=" + status);
    }
  }
}
