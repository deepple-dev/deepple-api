package kr.co.deepple.member.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.member.domain.ProfileImage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileImageFixture {

  private Long id;
  private long memberId = 1L;
  private Long imageId = 1L;
  private String imageKey = "images/1/profile.webp";
  private int order = 1;
  private boolean primary = true;

  public static ProfileImageFixture builder() {
    return new ProfileImageFixture();
  }

  public ProfileImage build() {
    ProfileImage profileImage =
        ProfileImage.builder()
            .memberId(memberId)
            .imageId(imageId)
            .imageKey(imageKey)
            .order(order)
            .isPrimary(primary)
            .build();
    setField(profileImage, "id", id);
    return profileImage;
  }
}
