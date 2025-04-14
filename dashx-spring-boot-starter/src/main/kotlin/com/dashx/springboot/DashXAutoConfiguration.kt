package com.dashx.springboot

import com.dashx.DashX
import com.dashx.DashXConfig
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(DashXProperties::class)
class DashXAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun dashxClient(properties: DashXProperties): DashX =
        DashX.getInstance().apply {
            configure(
                DashXConfig(
                    baseUrl = properties.baseUrl,
                    publicKey = properties.publicKey,
                    privateKey = properties.privateKey,
                    targetEnvironment = properties.targetEnvironment,
                ),
            )
        }
}
