package io.github.vonas.manko.util;

import com.google.common.base.Objects;

public class Pair<A, B> {

    protected final A first;
    protected final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?>))
            return false;

        Pair<?, ?> other = (Pair<?, ?>) o;
        return first.equals(other.first) && second.equals(other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(first, second);
    }
}
