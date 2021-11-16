package de.skuzzle.ghpromexporter.scrape;

@FunctionalInterface
interface ThrowingSupplier<T, E extends Throwable> {

    static <T, E extends Throwable> T unchecked(ThrowingSupplier<T, Throwable> sup) {
        return sup.getUnsafe();
    }

    T get() throws E;

    default T getUnsafe() {
        try {
            return get();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }
}