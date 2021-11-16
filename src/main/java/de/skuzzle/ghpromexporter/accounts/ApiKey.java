package de.skuzzle.ghpromexporter.accounts;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.springframework.http.server.reactive.ServerHttpRequest;

public sealed interface ApiKey {

    public static ApiKey fromToken(String token) {
        // TODO: parse token
        return new ValidApiKey(token, Duration.ofSeconds(5));
    }

    public static ApiKey fromRequest(ServerHttpRequest request) {
        final List<String> list = request.getQueryParams().get("apiKey");
        if (true || list == null || list.size() != 1) {
            final InetAddress source = request.getRemoteAddress().getAddress();
            return new IpBasedApiKey(source);
        }
        return fromToken(list.get(0));
    }

    Duration rateLimit();

    String subject();

    public final class ValidApiKey implements ApiKey {

        private final String subject;
        private final Duration rateLimit;

        private ValidApiKey(String subject, Duration rateLimit) {
            this.subject = subject;
            this.rateLimit = rateLimit;
        }

        @Override
        public String subject() {
            return subject;
        }

        @Override
        public Duration rateLimit() {
            return rateLimit;
        }

        @Override
        public int hashCode() {
            return Objects.hash(subject);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof final ValidApiKey other
                    && subject.equals(other.subject);
        }

    }

    public final class IpBasedApiKey implements ApiKey {

        private final InetAddress source;

        private IpBasedApiKey(InetAddress source) {
            this.source = source;
        }

        @Override
        public Duration rateLimit() {
            return Duration.ofMinutes(4);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof final IpBasedApiKey other
                    && source.equals(other.source);
        }

        @Override
        public String subject() {
            return source.toString();
        }

    }
}
