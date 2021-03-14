package de.j13g.manko.util;

import java.io.Serializable;

/**
 * Instance of this class are identifiable by their ID, and only by their ID.
 * Two instance of this class are considered equal when their IDs compare equal.
 * @param <I> The type of the ID.
 */
public abstract class Identifiable<I> implements Serializable {

    protected final I id;

    /**
     * Creates a new Identifiable instance with a specific ID.
     * The ID cannot change after, since instances cannot change their identity.
     * @param id The ID.
     */
    public Identifiable(I id) {
        this.id = id;
    }

    public final I id() {
        return id;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Identifiable<?>))
            return false;

        Identifiable<?> other = (Identifiable<?>) o;
        return id().equals(other.id());
    }

    @Override
    public final int hashCode() {
        return id().hashCode();
    }
}
