package kr.co.deepple.member.command.domain.member;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import lombok.Getter;

@Getter
public enum City {
    SEOUL("서울특별시"), INCHEON("인천광역시"), BUSAN("부산광역시"), DAEJEON("대전광역시"),
    DAEGU("대구광역시"), GWANGJU("광주광역시"), ULSAN("울산광역시"), JEJU("제주특별자치도"),
    SEJONG("세종특별자치도"), GANGWON("강원도"), GYEONGGI("경기도"), GYEONGSANGNAM("경상남도"),
    GYEONGSANGBUK("경상북도"), CHUNGCHEONGNAM("충청남도"), CHUNGCHEONGBUK("충청북도"), JEOLLANAM("전라남도"),
    JEOLLABUK("전라북도");

    private final String description;

    City(String description) {
        this.description = description;
    }

    public static City from(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return City.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_OPTION, "City=" + value, e);
        }
    }
}

