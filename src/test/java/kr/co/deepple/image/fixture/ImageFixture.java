package kr.co.deepple.image.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.domain.ImageContentType;
import kr.co.deepple.image.domain.ImageStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageFixture {

  private Long id;
  private long memberId = 1L;
  private Long adminId;
  private ImageContentType contentType = ImageContentType.WEBP;
  private ImageStatus status = ImageStatus.PROCESSING;

  public static ImageFixture builder() {
    return new ImageFixture();
  }

  public Image build() {
    Image image =
        adminId == null
            ? Image.uploadedByMember(memberId, contentType)
            : Image.uploadedByAdmin(adminId, contentType);
    setField(image, "id", id);
    setField(image, "status", status);
    return image;
  }
}
