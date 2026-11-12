package kr.co.deepple.member.command.application.member.sms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TestPhoneNumbersPropertiesTest {

    @DisplayName("설정이 없으면 꺼진 상태로 만들어져 어떤 입력도 통과하지 않는다.")
    @Test
    void disabledWhenNotConfigured() {
        TestPhoneNumbersProperties properties = bind(Map.of());

        assertThat(properties.accepts("01012345678", null)).isFalse();
        assertThat(properties.accepts("01012345678", "")).isFalse();
        assertThat(properties.accepts("01012345678", "008008")).isFalse();
    }

    @DisplayName("모든 번호를 테스트 번호로 두면 코드만 맞으면 통과한다.")
    @Test
    void allowsAll() {
        TestPhoneNumbersProperties properties = new TestPhoneNumbersProperties("008008", null, true);

        assertThat(properties.accepts("01012345678", "008008")).isTrue();
        assertThat(properties.accepts("01012345678", "123456")).isFalse();
    }

    @DisplayName("테스트 번호가 정해져 있으면 그 번호만 통과한다.")
    @Test
    void allowsListedNumbersOnly() {
        TestPhoneNumbersProperties properties = new TestPhoneNumbersProperties("135790", Set.of("01000000000"), false);

        assertThat(properties.accepts("01000000000", "135790")).isTrue();
        assertThat(properties.accepts("01012345678", "135790")).isFalse();
        assertThat(properties.accepts("01000000000", "008008")).isFalse();
    }

    @DisplayName("환경 변수처럼 쉼표로 이은 문자열을 테스트 번호 목록으로 읽는다.")
    @Test
    void bindsCommaSeparatedNumbers() {
        TestPhoneNumbersProperties properties = bind(Map.of(
            "auth.test-phone-numbers.verification-code", "135790",
            "auth.test-phone-numbers.numbers", "01000000000, 01011111111"
        ));

        assertThat(properties.numbers()).containsExactlyInAnyOrder("01000000000", "01011111111");
        assertThat(properties.allowAll()).isFalse();
    }

    @DisplayName("형식이 틀린 번호나 인증번호가 설정되면 앱이 시작하지 않는다.")
    @Test
    void failsToStartWithInvalidFormat() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(PropertiesConfig.class);

        runner.withPropertyValues("auth.test-phone-numbers.verification-code=135790",
                "auth.test-phone-numbers.numbers=01000000000")
            .run(context -> assertThat(context).hasNotFailed());
        runner.withPropertyValues("auth.test-phone-numbers.verification-code=135790",
                "auth.test-phone-numbers.numbers=010-0000-0000")
            .run(context -> assertThat(context).hasFailed());
        runner.withPropertyValues("auth.test-phone-numbers.verification-code=1357900")
            .run(context -> assertThat(context).hasFailed());
    }

    private TestPhoneNumbersProperties bind(Map<String, String> source) {
        return new Binder(new MapConfigurationPropertySource(source))
            .bindOrCreate("auth.test-phone-numbers", TestPhoneNumbersProperties.class);
    }

    @EnableConfigurationProperties(TestPhoneNumbersProperties.class)
    static class PropertiesConfig {
    }
}
