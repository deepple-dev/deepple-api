package deepple.deepple.common;

import deepple.deepple.common.event.Events;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.mockito.MockedStatic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

public class MockEventsExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private MockedStatic<Events> mockedEvents;

    @Override
    public void beforeEach(ExtensionContext context) {
        mockedEvents = mockStatic(Events.class);
        mockedEvents.when(() -> Events.raise(any())).then(invocation -> null);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        mockedEvents.close();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Type type = parameterContext.getParameter().getParameterizedType();
        return type instanceof ParameterizedType parameterized
            && parameterized.getRawType() == MockedStatic.class
            && parameterized.getActualTypeArguments()[0] == Events.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return mockedEvents;
    }
}
