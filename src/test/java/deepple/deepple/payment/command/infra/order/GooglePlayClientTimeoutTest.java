package deepple.deepple.payment.command.infra.order;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.sun.net.httpserver.HttpServer;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.payment.command.domain.PaymentErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Google Play 응답 지연 타임아웃 테스트")
class GooglePlayClientTimeoutTest {

    private static final Duration SERVER_DELAY = Duration.ofSeconds(30);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration MAX_WAIT = Duration.ofSeconds(7);

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
    @DisplayName("Google Play가 응답하지 않으면 읽기 타임아웃 안에 GOOGLE_PLAY_UNAVAILABLE로 실패한다")
    void failWithinReadTimeoutWhenGooglePlayDoesNotRespond() {
        // given
        AndroidPublisher androidPublisher = GooglePlayClientConfig.androidPublisherBuilder(request -> {
            }, "deepple-test", CONNECT_TIMEOUT, READ_TIMEOUT)
            .setRootUrl("http://localhost:" + slowServer.getAddress().getPort() + "/")
            .build();
        DefaultGooglePlayClient client = new DefaultGooglePlayClient(androidPublisher);
        ReflectionTestUtils.setField(client, "packageName", "deepple.test");

        // when
        long startedAt = System.nanoTime();
        assertThatThrownBy(() -> client.verifyPurchase("heart_45", "purchase-token"))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.GOOGLE_PLAY_UNAVAILABLE);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);

        // then
        assertThat(elapsed).isLessThan(MAX_WAIT);
    }
}
