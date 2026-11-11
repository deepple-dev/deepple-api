package kr.co.deepple.member.fixture;

import kr.co.deepple.member.command.domain.profileImage.ProfileImage;
import kr.co.deepple.member.command.domain.profileImage.vo.ImageUrl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileImageFixture {

    private Long id;
    private long memberId = 1L;
    private String imageUrl = "https://example.com/profile.jpg";
    private int order = 1;
    private boolean primary = true;

    public static ProfileImageFixture builder() {
        return new ProfileImageFixture();
    }

    public ProfileImage build() {
        ProfileImage profileImage = ProfileImage.builder()
            .memberId(memberId)
            .imageUrl(ImageUrl.from(imageUrl))
            .order(order)
            .isPrimary(primary)
            .build();
        setField(profileImage, "id", id);
        return profileImage;
    }
}
