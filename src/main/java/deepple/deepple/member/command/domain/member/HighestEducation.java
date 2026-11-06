package deepple.deepple.member.command.domain.member;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;


public enum HighestEducation {
    HIGH_SCHOOL("고등학교 졸업"),
    ASSOCIATE("전문대 졸업"),
    BACHELORS_LOCAL("지방 4년제 대학 졸업"),
    BACHELORS_SEOUL("서울 4년제 대학 졸업"),
    BACHELORS_OVERSEAS("해외 4년제 대학 졸업"),
    LAW_SCHOOL("로스쿨 졸업"),
    MASTERS("석사 졸업"),
    DOCTORATE("박사 졸업"),
    OTHER("기타");

    private final String description;

    HighestEducation(String description) {
        this.description = description;
    }

    public static HighestEducation from(String value) {
        if (value == null) {
            return null;
        }

        try {
            return HighestEducation.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_OPTION, "HighestEducation=" + value, e);
        }
    }
}
