package deepple.deepple.common.support;

import deepple.deepple.common.config.MySqlContainerConfig;
import deepple.deepple.common.config.S3ContainerConfig;
import deepple.deepple.common.config.ValkeyContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureMockMvc
@Import({MySqlContainerConfig.class, ValkeyContainerConfig.class, S3ContainerConfig.class})
public abstract class IntegrationTestSupport {
}
