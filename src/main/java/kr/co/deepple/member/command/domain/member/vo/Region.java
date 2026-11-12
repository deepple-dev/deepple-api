package kr.co.deepple.member.command.domain.member.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.District;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Region {

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private City city;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private District district;

  private Region(District district) {
    this.city = district.getCity();
    this.district = district;
  }

  public static Region from(District district) {
    if (district == null) {
      return null;
    }
    return new Region(district);
  }
}
