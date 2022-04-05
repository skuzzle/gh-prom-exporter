package de.skuzzle.ghpromexporter.web;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

@Component
class RegistrySerializer {

    private static final Logger log = LoggerFactory.getLogger(RegistrySerializer.class);

    static final MediaType OPEN_METRICS = MediaType
            .parseMediaType("application/openmetrics-text; version=1.0.0; charset=utf-8");

    static final MediaType TEXT_PLAIN_004 = MediaType.parseMediaType("text/plain; version=0.0.4; charset=utf-8");

    private static final List<Serializer> SUPPORTED_FORMATS = List.of(new TextPlainV004(), new OpenMetrics());

    /**
     * Determines the media type into which the prometheus registry is serialized. If the
     * client requested a type in the Accept header which is supported, that will be used.
     * Otherwise we fall back to text/plain.
     *
     * @param acceptibleMediaTypes The media type acceptable to the client.
     * @return The media type to return to the client.
     */
    public MediaType determineMediaType(Collection<MediaType> acceptibleMediaTypes) {
        final MediaType result = SUPPORTED_FORMATS.stream()
                .filter(serializer -> acceptibleMediaTypes.stream()
                        .anyMatch(acceptible -> serializer.supportedMediaType().isCompatibleWith(acceptible)))
                .findFirst()
                .map(Serializer::supportedMediaType)
                .orElse(TEXT_PLAIN_004);

        log.debug("Chose '{}' from acceptible meda types {}", result, acceptibleMediaTypes);
        return result;
    }

    public String serializeRegistry(CollectorRegistry registry, MediaType mediaType) {
        try (final var stringWriter = new StringWriter()) {
            SUPPORTED_FORMATS.stream()
                    .filter(serializer -> serializer.supportedMediaType().equals(mediaType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Can not serialize registry using target MediaType '%s'".formatted(mediaType)))
                    .write(stringWriter, registry.metricFamilySamples());

            return stringWriter.toString();
        } catch (final IOException e) {
            throw new IllegalStateException("Error while serializing registry", e);
        }
    }

    private interface Serializer {

        MediaType supportedMediaType();

        void write(Writer writer, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException;
    }

    private static class OpenMetrics implements Serializer {

        @Override
        public MediaType supportedMediaType() {
            return OPEN_METRICS;
        }

        @Override
        public void write(Writer writer, Enumeration<MetricFamilySamples> mfs) throws IOException {
            TextFormat.writeOpenMetrics100(writer, mfs);
        }

    }

    private static final class TextPlainV004 implements Serializer {

        @Override
        public MediaType supportedMediaType() {
            return TEXT_PLAIN_004;
        }

        @Override
        public void write(Writer writer, Enumeration<MetricFamilySamples> mfs) throws IOException {
            TextFormat.write004(writer, mfs);
        }

    }
}
