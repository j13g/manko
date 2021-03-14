package de.j13g.manko.util;

import java.io.Serializable;
import java.util.Objects;

public class Pair<A, B> implements Serializable {

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
        return Objects.hash(first, second);
    }
}
