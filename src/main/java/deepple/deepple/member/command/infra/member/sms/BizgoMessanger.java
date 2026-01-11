package deepple.deepple.member.command.infra.member.sms;

import deepple.deepple.member.command.infra.member.sms.dto.BizgoMessageRequest;
import deepple.deepple.member.command.infra.member.sms.exception.BizgoMessageSendException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
@RequiredArgsConstructor
public class BizgoMessanger {
    private final RestClient restClient;

    @Value("${bizgo.from-phone-number}")
    private String fromPhoneNumber;

    @Value("${bizgo.api-url}")
    private String apiUrl;

    @Value("${bizgo.api-key}")
    private String apiKey;

    public void sendMessage(String message, String phoneNumber) {
        /**
         * TODO : 타임아웃 에러에 대해서, Fallback 으로 재시도하는 로직 추가.
         */
        sendRequest(message, phoneNumber);
    }

    private void sendRequest(String message, String phoneNumber) {
        String requestURL = apiUrl + "/send/sms";

        restClient.post()
            .uri(requestURL)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Authorization", apiKey)
            .body(new BizgoMessageRequest(fromPhoneNumber, phoneNumber, message))
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
                    throw new BizgoMessageSendException(httpResponse.getStatusCode().value());
                }
            );
    }
}
