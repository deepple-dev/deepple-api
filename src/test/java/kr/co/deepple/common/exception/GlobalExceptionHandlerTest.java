package kr.co.deepple.common.exception;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    private MockMvcTester mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcTester.create(MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler(tracerProvider()))
            .build());
    }

    @Test
    @DisplayName("DeeppleException은 ErrorType에 맞는 상태와 ErrorCode의 code·메시지로 응답한다.")
    void respondDeeppleException() {
        MvcTestResult result = mockMvc.get()
            .uri("/deepple-exception")
            .exchange();

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(409);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("TEST_CONFLICT");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("테스트 충돌입니다.");
        assertThat(result).bodyJson().extractingPath("$.traceId").isEqualTo(TRACE_ID);
    }

    @Test
    @DisplayName("DeeppleException의 detail은 응답에 내려가지 않는다.")
    void hideDeeppleExceptionDetail() {
        MvcTestResult result = mockMvc.get()
            .uri("/deepple-exception")
            .exchange();

        assertThat(result).bodyText().doesNotContain("memberId=1");
    }

    @Test
    @DisplayName("DeeppleException에 담은 값은 extension 필드로 응답한다.")
    void respondDeeppleExceptionProperty() {
        MvcTestResult result = mockMvc.get()
            .uri("/deepple-exception-property")
            .exchange();

        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("TEST_CONFLICT");
        assertThat(result).bodyJson().extractingPath("$.retryAfter").isEqualTo("2026-11-07");
    }

    @Test
    @DisplayName("본문이 없으면 400으로 응답하고 내부 메시지를 노출하지 않는다.")
    void respondMissingBody() {
        MvcTestResult result = mockMvc.post()
            .uri("/body")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("BAD_REQUEST");
        assertThat(result).bodyText().doesNotContain("TestController", "TestRequest");
    }

    @Test
    @DisplayName("검증에 실패하면 400과 필드별 오류를 응답한다.")
    void respondValidationErrors() {
        MvcTestResult result = mockMvc.post()
            .uri("/body")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\"}")
            .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.errors[0].field").isEqualTo("name");
        assertThat(result).bodyJson().extractingPath("$.errors[0].message").isEqualTo("이름은 필수입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/optimistic-lock", "/cannot-acquire-lock"})
    @DisplayName("동시성 충돌은 409와 CONFLICT로 응답한다.")
    void respondConcurrencyFailure(String uri) {
        MvcTestResult result = mockMvc.get()
            .uri(uri)
            .exchange();

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CONFLICT");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("다른 요청과 동시에 처리되어 실패했습니다. 다시 시도해 주세요.");
    }

    @Test
    @DisplayName("예상하지 못한 예외는 500과 고정 메시지로 응답한다.")
    void respondUnexpectedException() {
        MvcTestResult result = mockMvc.get()
            .uri("/unexpected")
            .exchange();

        assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("서버 내부 오류가 발생했습니다.");
        assertThat(result).bodyText().doesNotContain("내부 비밀");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/illegal-argument", "/illegal-state"})
    @DisplayName("IllegalArgumentException·IllegalStateException은 코드 버그로 보고 500으로 응답한다.")
    void respondIllegalArgumentOrStateAsInternalError(String uri) {
        MvcTestResult result = mockMvc.get()
            .uri(uri)
            .exchange();

        assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(result).bodyText().doesNotContain("내부 상태");
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<Tracer> tracerProvider() {
        TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.traceId()).thenReturn(TRACE_ID);
        Span span = mock(Span.class);
        when(span.context()).thenReturn(traceContext);
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(span);
        ObjectProvider<Tracer> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(tracer);
        return provider;
    }

    @Getter
    @RequiredArgsConstructor
    private enum TestErrorCode implements ErrorCode {
        TEST_CONFLICT(ErrorType.ALREADY_EXISTS, "테스트 충돌입니다.");

        private final ErrorType type;
        private final String message;
    }

    private record TestRequest(@NotBlank(message = "이름은 필수입니다.") String name) {
    }

    @RestController
    private static class TestController {

        @GetMapping("/deepple-exception")
        void throwDeeppleException() {
            throw new DeeppleException(TestErrorCode.TEST_CONFLICT, "memberId=1");
        }

        @GetMapping("/deepple-exception-property")
        void throwDeeppleExceptionWithProperty() {
            throw new DeeppleException(TestErrorCode.TEST_CONFLICT).withProperty("retryAfter", "2026-11-07");
        }

        @PostMapping("/body")
        void receiveBody(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/optimistic-lock")
        void throwOptimisticLock() {
            throw new OptimisticLockingFailureException("버전 충돌");
        }

        @GetMapping("/cannot-acquire-lock")
        void throwCannotAcquireLock() {
            throw new CannotAcquireLockException("락 획득 실패");
        }

        @GetMapping("/illegal-argument")
        void throwIllegalArgument() {
            throw new IllegalArgumentException("내부 상태 값이 잘못됐습니다.");
        }

        @GetMapping("/illegal-state")
        void throwIllegalState() {
            throw new IllegalStateException("내부 상태가 잘못됐습니다.");
        }

        @GetMapping("/unexpected")
        void throwUnexpected() {
            throw new RuntimeException("내부 비밀");
        }
    }
}
