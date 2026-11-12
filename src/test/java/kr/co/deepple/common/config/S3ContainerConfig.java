package kr.co.deepple.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class S3ContainerConfig {

    public static final String BUCKET = "deepple-test";

    private static final int PORT = 9090;

    @Bean
    public GenericContainer<?> s3Container() {
        return new GenericContainer<>(ContainerImages.S3_MOCK)
            .withEnv("COM_ADOBE_TESTING_S3MOCK_STORE_INITIAL_BUCKETS", BUCKET)
            .withExposedPorts(PORT);
    }

    // S3Mock은 @ServiceConnection을 지원하지 않아 컨테이너 주소를 직접 넣는다
    @Bean
    public DynamicPropertyRegistrar s3Properties(GenericContainer<?> s3Container) {
        return registry -> {
            registry.add("spring.cloud.aws.s3.endpoint",
                () -> "http://%s:%d".formatted(s3Container.getHost(), s3Container.getMappedPort(PORT)));
            registry.add("spring.cloud.aws.s3.bucket", () -> BUCKET);
        };
    }
}
