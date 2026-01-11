package deepple.deepple.member.command.infra.member.sms;

import deepple.deepple.member.command.infra.member.sms.dto.BizgoMessageRequest;
import deepple.deepple.member.command.infra.member.sms.exception.BizgoMessageSendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
@RequiredArgsConstructor
@Slf4j
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
        String requestURL = apiUrl + "/api/comm/v1/send/omni";

        ResponseEntity<String> response = restClient.post()
            .uri(requestURL)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Authorization", apiKey)
            .body(new BizgoMessageRequest(message, fromPhoneNumber, phoneNumber))
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
                    throw new BizgoMessageSendException(httpResponse.getStatusCode().value());
                }
            ).toEntity(String.class);

        log.info("status = {}", response.getStatusCode());
        log.info("headers = {}", response.getHeaders());
        log.info("body = {}", response.getBody());
    }
}
