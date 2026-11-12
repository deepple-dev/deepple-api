package kr.co.deepple.member.command.application.member.sms;

import jakarta.validation.constraints.Pattern;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth.test-phone-numbers")
public record TestPhoneNumbersProperties(
    @Pattern(regexp = "^\\d{6}$", message = "테스트 인증번호는 6자리 숫자여야 합니다.") String verificationCode,
    Set<@Pattern(regexp = "^010\\d{8}$", message = "테스트 전화번호는 010으로 시작하는 11자리 숫자여야 합니다.") String>
        numbers,
    boolean allowAll) {
  public TestPhoneNumbersProperties {
    numbers = numbers == null ? Set.of() : Set.copyOf(numbers);
  }

  public boolean accepts(String phoneNumber, String code) {
    if (verificationCode == null || code == null || !isEqual(verificationCode, code)) {
      return false;
    }
    return allowAll || numbers.contains(phoneNumber);
  }

  private static boolean isEqual(String expected, String actual) {
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
  }
}
