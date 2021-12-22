package de.skuzzle.ghpromexporter.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.skuzzle.ghpromexporter.GhPromExporterApplication;

@Configuration(proxyBeanMethods = false)
class VersionConfiguration {

    private static final Logger log = LoggerFactory.getLogger(VersionConfiguration.class);

    @Bean
    Versions versions() {
        record VersionsImpl(String springBoot, String application) implements Versions {}

        final String application = getApplicationVersion(GhPromExporterApplication.class);
        final String springBoot = SpringBootVersion.getVersion();

        final Versions versions = new VersionsImpl(format(springBoot), format(application));
        log.info("Determined application version: {}", versions);
        return versions;
    }

    private String format(String version) {
        return version == null
                ? "<unknown>"
                : "v" + version;
    }

    private String getApplicationVersion(Class<?> sourceClass) {
        final Package sourcePackage = (sourceClass != null) ? sourceClass.getPackage() : null;
        return (sourcePackage != null) ? sourcePackage.getImplementationVersion() : null;
    }
}
