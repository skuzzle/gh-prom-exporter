package de.skuzzle.ghpromexporter.web;

import java.io.IOException;
import java.io.StringWriter;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

@Component
class RegistrySerializer {

    private static final MediaType OPEN_METRICS = MediaType
            .parseMediaType("application/openmetrics-text; version=1.0.0; charset=utf-8");

    public String serializeRegistry(CollectorRegistry registry, MediaType mediaType) {
        try (final var stringWriter = new StringWriter()) {
            if (mediaType.equals(OPEN_METRICS)) {
                TextFormat.writeOpenMetrics100(stringWriter, registry.metricFamilySamples());
            } else {
                TextFormat.write004(stringWriter, registry.metricFamilySamples());
            }
            return stringWriter.toString();
        } catch (final IOException e) {
            throw new IllegalStateException("Error while serializing registry", e);
        }
    }

}
