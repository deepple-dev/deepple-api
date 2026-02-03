package deepple.deepple.member.command.application.member.sms;

import deepple.deepple.member.command.application.member.exception.CodeNotExistsException;
import deepple.deepple.member.command.application.member.exception.CodeNotMatchException;
import deepple.deepple.member.command.infra.member.AuthMessageRedisRepository;
import deepple.deepple.member.command.infra.member.sms.BizgoMessanger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthMessageService {
    private static final Random generator = new SecureRandom();
    private final AuthMessageRedisRepository authMessageRedisRepository;
    private final BizgoMessanger bizgoMessanger;

    public void sendAndSaveCode(String phoneNumber) {
        String code = generateNumber();
        authMessageRedisRepository.save(phoneNumber, code);
        bizgoMessanger.sendMessage(getMessage(code), phoneNumber);
    }

    public void authenticate(String phoneNumber, String code) {
        String value = authMessageRedisRepository.getByKey(phoneNumber);
        validateWithCode(value, code);
        authMessageRedisRepository.delete(phoneNumber);
    }

    private String getMessage(String code) {
        return "[딥플] 인증번호 [" + code + "]를 입력해주세요.";
    }

    private String generateNumber() {
        return String.format("%06d", (generator.nextInt(1000000)));
    }

    private void validateWithCode(String value, String code) {
        if (value == null) {
            throw new CodeNotExistsException();
        }

        if (!value.equals(code)) {
            throw new CodeNotMatchException();
        }
    }
}
