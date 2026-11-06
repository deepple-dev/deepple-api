package deepple.deepple.member.command.domain.member.vo;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.query.member.AgeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Calendar;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
public class YearOfBirth {

    @Column(name = "year_of_birth")
    private Integer value;

    private YearOfBirth(Integer value) {
        if (value != null) {
            validateYearOfBirth(value);
        }
        this.value = value;
    }

    public static YearOfBirth from(Integer yearOfBirth) {
        return new YearOfBirth(yearOfBirth);
    }

    public Integer getAge() {
        if (value == null) {
            return null;
        }
        return AgeConverter.toAge(value);
    }

    private void validateYearOfBirth(Integer yearOfBirth) {
        int age = Calendar.getInstance().get(Calendar.YEAR) - yearOfBirth + 1;
        if (age < 20 || age > 46) {
            throw new DeeppleException(MemberErrorCode.INVALID_YEAR_OF_BIRTH, "yearOfBirth=" + yearOfBirth);
        }
    }
}
