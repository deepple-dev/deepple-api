package kr.co.deepple.image.domain;

import java.util.Arrays;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageContentType {
  JPEG("image/jpeg"),
  PNG("image/png"),
  WEBP("image/webp");

  private final String mimeType;

  public static ImageContentType from(String mimeType) {
    return Arrays.stream(values())
        .filter(type -> type.mimeType.equals(mimeType))
        .findFirst()
        .orElseThrow(() ->
            new DeeppleException(
                ImageErrorCode.UNSUPPORTED_IMAGE_CONTENT_TYPE, "contentType=" + mimeType));
  }
}
