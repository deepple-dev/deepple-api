package kr.co.deepple.heart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "heart_usage_policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeartUsagePolicy extends BaseEntity {

  private static final long MIN_PRICE = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private TransactionType transactionType;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Gender gender;

  private long price;

  private HeartUsagePolicy(
      @NonNull TransactionType transactionType, @NonNull Gender gender, long price) {
    if (!transactionType.isUsingType()) {
      throw new IllegalArgumentException(
          "하트를 사용하는 거래 종류가 아닙니다. transactionType: " + transactionType);
    }
    if (price < MIN_PRICE) {
      throw new IllegalArgumentException("price 값이 최소값보다 낮습니다. price=" + price);
    }
    this.transactionType = transactionType;
    this.gender = gender;
    this.price = price;
  }

  public static HeartUsagePolicy of(TransactionType transactionType, Gender gender, long price) {
    return new HeartUsagePolicy(transactionType, gender, price);
  }

  public boolean isFree(boolean isVip, TransactionSubtype subtype) {
    return isVip || subtype.isFreeFor(transactionType);
  }
}
