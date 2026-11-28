package kr.co.deepple.recommendation.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 이상형 나이를 코드에서는 연 나이(올해 − 태어난 해)로, DB({@code member_ideals.min_age}·{@code max_age})에는 한국식 나이(연 나이
 * + 1)로 둔다.
 *
 * <p>DB 값을 한 살씩 옮기는 마이그레이션은 배포 중 함께 도는 옛 버전(한국식으로 읽고 쓰는 코드)과 롤백한 버전이 값을 잘못 읽고 쓰게 만든다. 컬럼 값의 뜻을 그대로
 * 두고 읽고 쓸 때 바꾸면 옛 버전과 새 버전이 같은 값을 같은 태어난 해로 해석한다.
 */
@Converter
class KoreanAgeColumnConverter implements AttributeConverter<Integer, Integer> {

  @Override
  public Integer convertToDatabaseColumn(Integer yearAge) {
    return yearAge == null ? null : yearAge + 1;
  }

  @Override
  public Integer convertToEntityAttribute(Integer koreanAge) {
    return koreanAge == null ? null : koreanAge - 1;
  }
}
