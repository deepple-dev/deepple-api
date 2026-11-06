package deepple.deepple.member.command.domain.profileImage.vo;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@EqualsAndHashCode
public class ImageUrl {

    @Column(name = "url")
    private final String value;

    private ImageUrl(String value) {
        validateUrl(value);
        this.value = value;
    }

    public static ImageUrl from(String value) {
        return new ImageUrl(value);
    }

    private void validateUrl(String value) {
        if (value == null || value.isEmpty()) {
            throw new DeeppleException(MemberErrorCode.INVALID_PROFILE_IMAGE_URL);
        }
    }

}
