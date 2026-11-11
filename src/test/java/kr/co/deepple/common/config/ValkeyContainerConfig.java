package kr.co.deepple.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class ValkeyContainerConfig {

    private static final int PORT = 6379;

    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> valkeyContainer() {
        return new GenericContainer<>(ContainerImages.VALKEY)
            .withExposedPorts(PORT);
    }
}
