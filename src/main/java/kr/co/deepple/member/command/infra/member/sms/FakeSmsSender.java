package kr.co.deepple.member.command.infra.member.sms;

import kr.co.deepple.member.command.domain.sms.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "bizgo.enabled", havingValue = "false")
public class FakeSmsSender implements SmsSender {

    @Override
    public void sendMessage(String message, String phoneNumber) {
        log.info("[문자 발송] to={}, message={}", phoneNumber, message);
    }
}
