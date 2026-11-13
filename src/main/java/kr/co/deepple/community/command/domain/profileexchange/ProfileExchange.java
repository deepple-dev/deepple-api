package kr.co.deepple.community.command.domain.profileexchange;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.command.domain.CommunityErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "profile_exchanges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    if (requesterId == responderId) {
      throw new DeeppleException(
          CommunityErrorCode.SELF_PROFILE_EXCHANGE, "memberId=" + requesterId);
    }
    this.requesterId = requesterId;
    this.responderId = responderId;
    this.status = status;
  }

  public static ProfileExchange request(long requesterId, long responderId) {
    return new ProfileExchange(requesterId, responderId, ProfileExchangeStatus.WAITING);
  }

  public void approve() {
    validateWaitingStatus();
    status = ProfileExchangeStatus.APPROVE;
  }

  public void reject() {
    validateWaitingStatus();
    status = ProfileExchangeStatus.REJECTED;
  }

  private void validateWaitingStatus() {
    if (status != ProfileExchangeStatus.WAITING) {
      throw new DeeppleException(
          CommunityErrorCode.INVALID_PROFILE_EXCHANGE_STATUS,
          "profileExchangeId=" + id + ", status=" + status);
    }
  }
}
