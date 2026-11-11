package deepple.deepple.member.command.infra.member.sms;

import com.sun.net.httpserver.HttpServer;
import deepple.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RestClient 응답 지연 타임아웃 통합 테스트")
class RestClientTimeoutIntegrationTest extends IntegrationTestSupport {

    private static final Duration SERVER_DELAY = Duration.ofSeconds(30);
    private static final Duration MAX_WAIT = Duration.ofSeconds(5);

    @Autowired
    private RestClient.Builder restClientBuilder;

    private HttpServer slowServer;

    @BeforeEach
    void setUp() throws IOException {
        slowServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        slowServer.setExecutor(Executors.newCachedThreadPool());
        slowServer.createContext("/", exchange -> {
            try {
                Thread.sleep(SERVER_DELAY);
                exchange.sendResponseHeaders(200, -1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        slowServer.start();
    }

    @AfterEach
    void tearDown() {
        slowServer.stop(0);
    }

    @Test
    @DisplayName("외부 서버가 응답하지 않으면 읽기 타임아웃 안에 실패한다")
    void failWithinReadTimeoutWhenServerDoesNotRespond() {
        // when
        long startedAt = System.nanoTime();
        assertThatThrownBy(() -> restClientBuilder.build().get()
            .uri("http://localhost:" + slowServer.getAddress().getPort() + "/")
            .retrieve()
            .toBodilessEntity())
            .isInstanceOf(ResourceAccessException.class);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

        // then
        assertThat(elapsed).isLessThan(MAX_WAIT);
    }
}
