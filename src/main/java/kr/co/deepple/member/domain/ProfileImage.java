package kr.co.deepple.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profile_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  private Long imageId;

  private String imageKey;

  private boolean isPrimary;

  @Column(name = "profile_order")
  private int order;

  @Builder
  private ProfileImage(long memberId, Long imageId, String imageKey, int order, boolean isPrimary) {
    this.memberId = memberId;
    this.imageId = imageId;
    this.imageKey = imageKey;
    setOrder(order);
    this.isPrimary = isPrimary;
  }

  private void setOrder(int order) {
    if (order < 0) {
      throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_IMAGE_ORDER, "order=" + order);
    }
    this.order = order;
  }

  public void updateOrderAndPrimary(int order, boolean isPrimary) {
    setOrder(order);
    this.isPrimary = isPrimary;
  }
}
