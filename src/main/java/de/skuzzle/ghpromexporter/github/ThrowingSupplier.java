package de.skuzzle.ghpromexporter.github;

import java.util.Optional;

@FunctionalInterface
interface ThrowingSupplier<T, E extends Exception> {

    static <T, E extends Throwable> T unchecked(ThrowingSupplier<T, Exception> sup) {
        return sup.getUnsafe();
    }

    static <T, E extends Throwable> Optional<T> optional(ThrowingSupplier<T, Exception> sup) {
        return sup.optional();
    }

    T get() throws E;

    default T getUnsafe() {
        try {
            return get();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    default Optional<T> optional() {
        try {
            final T t = get();
            return Optional.ofNullable(t);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }
}