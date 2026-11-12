package kr.co.deepple;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableJpaAuditing
public class DeeppleApplication {

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    SpringApplication.run(DeeppleApplication.class, args);
  }
}
