package deepple.deepple.member.command.infra.member.sms;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class BizgoMessengerTest {

    private static final String API_URL = "https://bizgo.test";

    private MockRestServiceServer server;
    private BizgoMessenger bizgoMessenger;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        bizgoMessenger = new BizgoMessenger(builder, JsonMapper.builder().build());
        ReflectionTestUtils.setField(bizgoMessenger, "apiUrl", API_URL);
        ReflectionTestUtils.setField(bizgoMessenger, "apiKey", "key");
        ReflectionTestUtils.setField(bizgoMessenger, "fromPhoneNumber", "0200000000");
    }

    @Test
    @DisplayName("실패 응답의 data.code를 예외 detail에 담는다.")
    void putNestedErrorCodeInDetail() {
        respond(HttpStatus.BAD_REQUEST, """
            {"common": {"authCode": "A000"}, "data": {"code": "A327", "result": "Not Allowed 'from'"}}
            """);

        assertThatThrownBy(() -> bizgoMessenger.sendMessage("인증번호", "01000000000"))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.SMS_SEND_FAILED)
            .hasMessage("Bizgo statusCode=400, code=A327");
    }

    @Test
    @DisplayName("data 없이 최상위에 code가 오는 실패 응답도 오류 코드를 담는다.")
    void putFlatErrorCodeInDetail() {
        respond(HttpStatus.UNAUTHORIZED, """
            {"code": "A001", "result": "Expired token"}
            """);

        assertThatThrownBy(() -> bizgoMessenger.sendMessage("인증번호", "01000000000"))
            .isInstanceOf(DeeppleException.class)
            .hasMessage("Bizgo statusCode=401, code=A001");
    }

    @Test
    @DisplayName("실패 응답을 읽을 수 없으면 원문 대신 unknown을 담는다.")
    void putUnknownWhenBodyIsNotJson() {
        respond(HttpStatus.INTERNAL_SERVER_ERROR, "<html>error 01000000000</html>");

        assertThatThrownBy(() -> bizgoMessenger.sendMessage("인증번호", "01000000000"))
            .isInstanceOf(DeeppleException.class)
            .hasMessage("Bizgo statusCode=500, code=unknown");
    }

    private void respond(HttpStatus status, String body) {
        server.expect(requestTo(API_URL + "/api/comm/v1/send/omni"))
            .andRespond(withStatus(status).contentType(MediaType.APPLICATION_JSON).body(body));
    }
}
