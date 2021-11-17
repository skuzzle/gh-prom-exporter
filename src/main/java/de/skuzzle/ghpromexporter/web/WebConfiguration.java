package de.skuzzle.ghpromexporter.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WebProperties.class)
public class WebConfiguration {

    @Bean
    SerializedRegistryCache cachingRegistrySerializer(WebProperties properties) {
        return new SerializedRegistryCache(properties.serializedRegistryCache().buildCache());
    }
}
