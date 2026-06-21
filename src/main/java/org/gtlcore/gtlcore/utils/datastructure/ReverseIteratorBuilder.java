package org.gtlcore.gtlcore.utils.datastructure;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public class ReverseIteratorBuilder {

    @NotNull
    public static <T> ObjectIterator<T> build(@NotNull ReferenceSortedSet<@NotNull T> set) {
        return set.isEmpty() ? ObjectIterators.emptyIterator() : set.size() == 1 ? ObjectIterators.singleton(set.first()) : new ReverseIterator<>(set);
    }

    private static class ReverseIterator<T> implements ObjectIterator<T> {

        private final ObjectBidirectionalIterator<T> delegate;

        // never use empty set
        public ReverseIterator(ReferenceSortedSet<T> set) {
            this.delegate = set.iterator(set.last());
        }

        @Override
        public boolean hasNext() {
            return delegate.hasPrevious();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return delegate.previous();
        }

        @Override
        public void remove() {
            delegate.remove();
        }
    }
}
