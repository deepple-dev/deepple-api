package deepple.deepple.common.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Slf4j
@Configuration
@Profile("!test")
@EnableConfigurationProperties(org.springframework.boot.autoconfigure.flyway.FlywayProperties.class)
public class FlywayConfig {
    @Bean
    public Flyway flyway(DataSource dataSource, FlywayProperties props) {
        return Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(props.isBaselineOnMigrate())
            .baselineVersion(MigrationVersion.fromVersion(props.getBaselineVersion()))
            .locations(props.getLocations().toArray(new String[0]))
            .table(props.getTable())
            .load();
    }

    @Bean
    public ApplicationRunner migrateFlyway(Flyway flyway, FlywayProperties props) {
        return args -> {
            if (!props.isEnabled()) {
                log.info("Flyway is disabled. Skipping migration.");
                return;
            }
            try {
                flyway.migrate();
            } catch (Exception e) {
                log.error("Flyway migration failed", e);
            }
        };
    }
}