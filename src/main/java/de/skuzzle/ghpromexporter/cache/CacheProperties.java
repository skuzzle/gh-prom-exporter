package de.skuzzle.ghpromexporter.cache;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;

/**
 * Can be used to configure Guava caches using Spring Configuration Properties.
 */
public final class CacheProperties {

    private static final int INT_DEFAULT = -1;

    private int initialCapacity = INT_DEFAULT;
    private int maximumSize = INT_DEFAULT;
    private int concurrencyLevel = INT_DEFAULT;
    private Duration expireAfterAccess;
    private Duration expireAfterWrite;
    private Duration refreshAfterWrite;

    public CacheBuilder<Object, Object> newBuilder() {
        final CacheBuilder<Object, Object> newBuilder = CacheBuilder.newBuilder();
        configure(newBuilder);
        return newBuilder;
    }

    public <K, V> void configure(CacheBuilder<K, V> builder) {
        applyInt(maximumSize, builder::maximumSize);
        applyInt(initialCapacity, builder::initialCapacity);
        applyInt(concurrencyLevel, builder::concurrencyLevel);
        apply(expireAfterAccess, builder::expireAfterAccess);
        apply(expireAfterWrite, builder::expireAfterWrite);
        apply(refreshAfterWrite, builder::refreshAfterWrite);
    }

    private void applyInt(int value, IntConsumer target) {
        Preconditions.checkArgument(value != 0, "value must be postive");
        if (value != INT_DEFAULT) {
            target.accept(value);
        }
    }

    private <T> void apply(T value, Consumer<? super T> target) {
        if (value != null) {
            target.accept(value);
        }
    }

    public CacheProperties setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
        return this;
    }

    public CacheProperties setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
        return this;
    }

    public CacheProperties setConcurrencyLevel(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
        return this;
    }

    public CacheProperties setExpireAfterAccess(Duration expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
        return this;
    }

    public CacheProperties setExpireAfterWrite(Duration expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
        return this;
    }

    public CacheProperties setRefreshAfterWrite(Duration refreshAfterWrite) {
        this.refreshAfterWrite = refreshAfterWrite;
        return this;
    }
}
