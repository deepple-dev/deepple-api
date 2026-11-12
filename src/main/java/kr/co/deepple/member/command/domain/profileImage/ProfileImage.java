package kr.co.deepple.member.command.domain.profileImage;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.profileImage.vo.ImageUrl;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(
    name = "profile_images",
    indexes = {@Index(name = "idx_member_id_is_primary", columnList = "memberId, isPrimary")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileImage extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  private Long id;

  @Getter private Long memberId;

  @Embedded private ImageUrl imageUrl;

  private boolean isPrimary;

  @Column(name = "profile_order")
  private int order;

  @Builder
  private ProfileImage(Long memberId, ImageUrl imageUrl, int order, boolean isPrimary) {
    setMemberId(memberId);
    setImageUrl(imageUrl);
    setOrder(order);
    setPrimary(isPrimary);
  }

  public String getUrl() {
    return imageUrl.getValue();
  }

  public Integer getOrder() {
    return order;
  }

  private void setOrder(int order) {
    validateOrder(order);
    this.order = order;
  }

  public Boolean isPrimary() {
    return isPrimary;
  }

  public void updateOrderAndPrimary(int order, boolean isPrimary) {
    setOrder(order);
    setPrimary(isPrimary);
  }

  public void updateUrl(String imageUrl) {
    if (imageUrl != null) {
      setImageUrl(ImageUrl.from(imageUrl));
    }
  }

  private void setMemberId(@NonNull Long memberId) {
    this.memberId = memberId;
  }

  private void setPrimary(boolean isPrimary) {
    this.isPrimary = isPrimary;
  }

  private void setImageUrl(ImageUrl imageUrl) {
    this.imageUrl = imageUrl;
  }

  private void validateOrder(int order) {
    if (order < 0) {
      throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_IMAGE_ORDER, "order=" + order);
    }
  }
}
