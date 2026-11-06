package deepple.deepple.common.config;

import deepple.deepple.common.event.Event;
import deepple.deepple.common.logging.LogContext;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.integration.Slf4jThreadLocalAccessor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    static {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new Slf4jThreadLocalAccessor(LogContext.KEYS));
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setThreadNamePrefix("custom-async-");
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (exception, method, params) -> {
            String previous = LogContext.putEventAction(LogContext.resolveEventAction(method.getName(), params));
            try {
                LoggingEventBuilder event = log.atError()
                    .setCause(exception)
                    .addKeyValue("code.function.name",
                        method.getDeclaringClass().getSimpleName() + "." + method.getName());
                if (params.length > 0 && params[0] instanceof Event domainEvent) {
                    for (Map.Entry<String, Object> attribute : domainEvent.logAttributes().entrySet()) {
                        event = event.addKeyValue(attribute.getKey(), attribute.getValue());
                    }
                }
                event.log("비동기 작업 실패");
            } finally {
                LogContext.restoreEventAction(previous);
            }
        };
    }
}
