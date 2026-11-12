package kr.co.deepple.community.command.domain.profileexchange;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.command.domain.CommunityErrorCode;
import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeAcceptedEvent;
import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeRejectedEvent;
import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeRequestedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "profile_exchanges",
    indexes = {
      @Index(name = "idx_responder_id", columnList = "responderId"),
      @Index(name = "idx_requester_id_responder_id", columnList = "requesterId, responderId")
    })
public class ProfileExchange extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long requesterId;

  private long responderId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private ProfileExchangeStatus status;

  private ProfileExchange(
      long requesterId, long responderId, @NonNull ProfileExchangeStatus status) {
    validateRequesterIdAndResponderId(requesterId, responderId);
    this.requesterId = requesterId;
    this.responderId = responderId;
    this.status = status;
  }

  public static ProfileExchange request(long requesterId, long responderId, String senderName) {
    Events.raise(ProfileExchangeRequestedEvent.of(requesterId, responderId, senderName));
    return new ProfileExchange(requesterId, responderId, ProfileExchangeStatus.WAITING);
  }

  public void approve(String senderName) {
    validateWaitingStatus();
    Events.raise(ProfileExchangeAcceptedEvent.of(requesterId, responderId, senderName));
    status = ProfileExchangeStatus.APPROVE;
  }

  public void reject(String senderName) {
    validateWaitingStatus();
    Events.raise(ProfileExchangeRejectedEvent.of(requesterId, responderId, senderName));
    status = ProfileExchangeStatus.REJECTED;
  }

  private void validateRequesterIdAndResponderId(long requesterId, long responderId) {
    if (requesterId == responderId) {
      throw new DeeppleException(
          CommunityErrorCode.SELF_PROFILE_EXCHANGE, "memberId=" + requesterId);
    }
  }

  private void validateWaitingStatus() {
    if (status != ProfileExchangeStatus.WAITING) {
      throw new DeeppleException(
          CommunityErrorCode.INVALID_PROFILE_EXCHANGE_STATUS,
          "profileExchangeId=" + id + ", status=" + status);
    }
  }
}
