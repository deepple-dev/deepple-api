package kr.co.deepple.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlContainerConfig {

    @Bean
    @ServiceConnection
    public MySQLContainer mySqlContainer() {
        return new MySQLContainer(ContainerImages.MYSQL).withEnv("TZ", "Asia/Seoul");
    }
}
