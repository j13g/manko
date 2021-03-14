package de.j13g.manko.core;

import de.j13g.manko.util.UniformPair;

import java.io.Serializable;
import java.util.Objects;

/**
 * A pairing between two entrants.
 * @param <E> The entrant type.
 */
public class Pairing<E> extends UniformPair<E> implements Serializable {

    /**
     * Creates a pairing between two entrants.
     * @param first The first entrant.
     * @param second The second entrant.
     */
    public Pairing(E first, E second) {
        super(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pairing<?>))
            return false;

        Pairing<?> other = (Pairing<?>) o;
        return first.equals(other.first) && second.equals(other.second)
            || first.equals(other.second) && second.equals(other.first);
    }

    @Override
    public int hashCode() {
        // hash(a, b) + hash(b, a) == hash(b, a) + hash(a, b)
        return Objects.hash(first, second) + Objects.hash(second, first);
    }
}
