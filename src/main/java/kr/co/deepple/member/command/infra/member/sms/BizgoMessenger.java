package kr.co.deepple.member.command.infra.member.sms;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.sms.SmsSender;
import kr.co.deepple.member.command.infra.member.sms.dto.BizgoMessageRequest;
import kr.co.deepple.member.command.infra.member.sms.dto.BizgoMessageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


@Service
@ConditionalOnProperty(name = "bizgo.enabled", havingValue = "true", matchIfMissing = true)
public class BizgoMessenger implements SmsSender {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public BizgoMessenger(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Value("${bizgo.from-phone-number}")
    private String fromPhoneNumber;

    @Value("${bizgo.api-url}")
    private String apiUrl;

    @Value("${bizgo.api-key}")
    private String apiKey;

    @Override
    public void sendMessage(String message, String phoneNumber) {
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
            String code = response.data() == null ? response.code() : response.data().code();
            return "code=" + code;
        } catch (JacksonException e) {
            return "code=unknown";
        }
    }
}
