package kr.co.deepple.common.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// compose.yml과 같은 버전을 쓴다. 올릴 때 함께 바꾼다
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContainerImages {

  public static final String MYSQL = "mysql:8.4.13";
  public static final String VALKEY = "valkey/valkey:8.1.11";
  public static final String S3_MOCK = "adobe/s3mock:5.2.3";
}
