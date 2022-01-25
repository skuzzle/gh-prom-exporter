package org.kohsuke.github;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is needed in this package to provide public access to otherwise package
 * private PagedIterator constructor.
 *
 * @author Simon Taddiken
 */
public final class PagedIterables {

    private PagedIterables() {
    }

    private static <T> void doNothing(T t) {

    }

    @SafeVarargs
    public static <T> PagedIterable<T> of(T... valuesInFirstPage) {
        final List<T[]> singlePage = new ArrayList<>();
        singlePage.add(valuesInFirstPage);
        return new PagedIterable<T>() {

            @Override
            public PagedIterator<T> _iterator(int pageSize) {
                return new PagedIterator<T>(singlePage.iterator(), PagedIterables::doNothing);
            }
        };
    }
}
