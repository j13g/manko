package de.j13g.manko.util;

import de.j13g.manko.util.exceptions.NoSuchElementException;

public class UniformPair<E> extends Pair<E, E> {

    public UniformPair(E first, E second) {
        super(first, second);
    }

    public E getOther(E element) throws NoSuchElementException {
        if (!contains(element))
            throw new NoSuchElementException();

        return element == first ? second : first;
    }

    public boolean contains(E element) {
        return element == first || element == second;
    }

    public boolean containsEqual(E element) {
        return element.equals(first) || element.equals(second);
    }
}
