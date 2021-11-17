package de.skuzzle.ghpromexporter.github;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GitHubProperties.class)
public class GitHubConfiguration {

    private final GitHubProperties properties;

    public GitHubConfiguration(GitHubProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void configureCache() {
        GitHubFactory.CACHED_GITHUBS = properties.cache().buildCache();
    }
}
