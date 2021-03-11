package de.j13g.manko.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public abstract class BaseSet<E> implements Serializable {

    protected final Set<E> elements;

    protected BaseSet(Set<E> elements) {
        this.elements = elements;
    }

    public boolean add(E element) {
        return elements.add(element);
    }

    public boolean remove(E element) {
        return elements.remove(element);
    }

    public void clear() {
        elements.clear();
    }

    /**
     * Returns a view of the elements.
     * @see Collections#unmodifiableSet(Set)
     * @return The elements.
     */
    public Set<E> elements() {
        return Collections.unmodifiableSet(elements);
    }

    public int size() {
        return elements.size();
    }

    public boolean contains(E element) {
        return elements.contains(element);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseSet<?>))
            return false;

        BaseSet<?> other = (BaseSet<?>)o;
        return elements.equals(other.elements);
    }
}
