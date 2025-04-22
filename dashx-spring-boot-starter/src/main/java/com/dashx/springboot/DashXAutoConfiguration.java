package com.dashx.springboot;

import com.dashx.DashX;
import com.dashx.DashXConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(DashXProperties.class)
public class DashXAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public DashX dashXClient(DashXProperties properties) {
        DashXConfig config = new DashXConfig.Builder().baseUrl(properties.getBaseUrl())
                .publicKey(properties.getPublicKey()).privateKey(properties.getPrivateKey())
                .targetEnvironment(properties.getTargetEnvironment()).build();

        DashX client = DashX.getInstance();
        client.configure(config);

        return client;
    }
}
