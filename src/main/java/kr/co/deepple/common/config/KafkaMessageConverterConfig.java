package kr.co.deepple.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.ByteArrayJacksonJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper.TypePrecedence;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class KafkaMessageConverterConfig {

  private static final String EVENT_PACKAGES = "kr.co.deepple.*.event";

  @Bean
  public RecordMessageConverter kafkaMessageConverter(JsonMapper jsonMapper) {
    DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
    typeMapper.setTypePrecedence(TypePrecedence.TYPE_ID);
    typeMapper.addTrustedPackages(EVENT_PACKAGES);

    ByteArrayJacksonJsonMessageConverter converter =
        new ByteArrayJacksonJsonMessageConverter(jsonMapper);
    converter.setTypeMapper(typeMapper);
    return converter;
  }
}
