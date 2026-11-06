package deepple.deepple.member.command.infra.member.sms;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.sms.SmsSender;
import deepple.deepple.member.command.infra.member.sms.dto.BizgoMessageRequest;
import deepple.deepple.member.command.infra.member.sms.dto.BizgoMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


@Service
@ConditionalOnProperty(name = "bizgo.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class BizgoMessenger implements SmsSender {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${bizgo.from-phone-number}")
    private String fromPhoneNumber;

    @Value("${bizgo.api-url}")
    private String apiUrl;

    @Value("${bizgo.api-key}")
    private String apiKey;

    @Override
    public void sendMessage(String message, String phoneNumber) {
        /**
         * TODO : 타임아웃 에러에 대해서, Fallback 으로 재시도하는 로직 추가.
         */
        sendRequest(message, phoneNumber);
    }

    private void sendRequest(String message, String phoneNumber) {
        String requestURL = apiUrl + "/api/comm/v1/send/omni";

        restClient.post()
            .uri(requestURL)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Authorization", apiKey)
            .body(new BizgoMessageRequest(message, fromPhoneNumber, phoneNumber))
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
                    throw new DeeppleException(
                        MemberErrorCode.SMS_SEND_FAILED,
                        "Bizgo statusCode=" + httpResponse.getStatusCode().value()
                            + ", " + extractErrorCode(httpResponse.getBody().readAllBytes())
                    );
                }
            )
            .toBodilessEntity();
    }

    private String extractErrorCode(byte[] body) {
        try {
            BizgoMessageResponse response = objectMapper.readValue(body, BizgoMessageResponse.class);
            String authCode = response.common() == null ? null : response.common().authCode();
            String code = response.data() == null ? null : response.data().code();
            return "authCode=" + authCode + ", code=" + code;
        } catch (JacksonException e) {
            return "errorCode=unknown";
        }
    }
}
